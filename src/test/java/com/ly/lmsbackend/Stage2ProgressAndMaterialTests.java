package com.ly.lmsbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.lmsbackend.controller.LessonMaterialController;
import com.ly.lmsbackend.controller.LessonProgressController;
import com.ly.lmsbackend.dto.lessondtos.*;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.FileUploadService;
import com.ly.lmsbackend.service.LessonMaterialService;
import com.ly.lmsbackend.service.LessonProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class Stage2ProgressAndMaterialTests {

    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private LessonMaterialRepository lessonMaterialRepository;
    @Mock private FileUploadService fileUploadService;

    private LessonProgressService lessonProgressService;
    private LessonMaterialService lessonMaterialService;

    private MockMvc progressMockMvc;
    private MockMvc materialMockMvc;
    private ObjectMapper objectMapper;

    private Users user;
    private Students student;
    private Courses course;
    private Sections section;
    private Lessons lesson1;
    private Lessons lesson2;
    private Lessons lesson3;

    @BeforeEach
    void setUp() {
        lessonProgressService = new LessonProgressService(
                lessonProgressRepository,
                lessonRepository,
                studentRepository,
                enrollmentRepository,
                courseRepository,
                userRepository
        );

        lessonMaterialService = new LessonMaterialService(
                lessonMaterialRepository,
                lessonRepository,
                studentRepository,
                enrollmentRepository,
                userRepository,
                fileUploadService
        );

        objectMapper = new ObjectMapper();

        LessonProgressController progressController = new LessonProgressController(lessonProgressService);
        LessonMaterialController materialController = new LessonMaterialController(lessonMaterialService);

        progressMockMvc = MockMvcBuilders.standaloneSetup(progressController).build();
        materialMockMvc = MockMvcBuilders.standaloneSetup(materialController).build();

        user = Users.builder()
                .id(1L)
                .username("student1")
                .email("student@test.com")
                .role(Roles.STUDENT)
                .build();

        student = Students.builder()
                .id(10L)
                .user(user)
                .fullName("Test Student")
                .studentCode("STU-2026-0001")
                .build();

        course = new Courses();
        course.setCourseId(100L);
        course.setTitle("Java Masterclass");

        section = new Sections();
        section.setSectionId(200L);
        section.setTitle("Basics");
        section.setCourse(course);

        lesson1 = new Lessons();
        lesson1.setLessonId(1L);
        lesson1.setTitle("Introduction");
        lesson1.setOrderIndex(0);
        lesson1.setSection(section);

        lesson2 = new Lessons();
        lesson2.setLessonId(2L);
        lesson2.setTitle("Variables");
        lesson2.setOrderIndex(1);
        lesson2.setSection(section);

        lesson3 = new Lessons();
        lesson3.setLessonId(3L);
        lesson3.setTitle("Loops");
        lesson3.setOrderIndex(2);
        lesson3.setSection(section);
    }

    private void mockActiveEnrollment() {
        when(studentRepository.findByUser_Email("student@test.com")).thenReturn(Optional.of(student));
        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 100L)).thenReturn(Optional.of(enrollment));
    }

    // ==========================================
    // 1. Mark Lesson as Complete
    // ==========================================

    @Test
    @DisplayName("markComplete marks lesson as completed and sets completedAt")
    void testMarkComplete() {
        mockActiveEnrollment();
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonProgressRepository.findByStudentAndLesson(student, lesson1)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(invocation -> {
            LessonProgress lp = invocation.getArgument(0);
            lp.setId(1L);
            return lp;
        });

        LessonProgressDTO result = lessonProgressService.markComplete(1L, "student@test.com");

        assertNotNull(result);
        assertEquals(1L, result.lessonId());
        assertTrue(result.isCompleted());
        assertNotNull(result.completedAt());
        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    // ==========================================
    // 2. Video Playback Progress
    // ==========================================

    @Test
    @DisplayName("savePlaybackPosition updates lastPlaybackPositionSeconds")
    void testSavePlaybackPosition() {
        mockActiveEnrollment();
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonProgressRepository.findByStudentAndLesson(student, lesson1)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(invocation -> {
            LessonProgress lp = invocation.getArgument(0);
            lp.setId(2L);
            return lp;
        });

        LessonProgressDTO result = lessonProgressService.savePlaybackPosition(1L, 45.5, "student@test.com");

        assertNotNull(result);
        assertEquals(1L, result.lessonId());
        assertEquals(45.5, result.lastPlaybackPositionSeconds());
        assertFalse(result.isCompleted());
        verify(lessonProgressRepository).save(any(LessonProgress.class));
    }

    @Test
    @DisplayName("getProgress returns default when no progress exists")
    void testGetProgressDefault() {
        mockActiveEnrollment();
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonProgressRepository.findByStudentAndLesson(student, lesson1)).thenReturn(Optional.empty());

        LessonProgressDTO result = lessonProgressService.getProgress(1L, "student@test.com");

        assertNotNull(result);
        assertEquals(1L, result.lessonId());
        assertFalse(result.isCompleted());
        assertEquals(0.0, result.lastPlaybackPositionSeconds());
    }

    // ==========================================
    // 3. Navigation (Previous / Next)
    // ==========================================

    @Test
    @DisplayName("getNavigation correctly calculates previous and next lesson IDs")
    void testGetNavigation() {
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(lesson2));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Email("student@test.com")).thenReturn(Optional.of(student));
        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 100L)).thenReturn(Optional.of(enrollment));

        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(100L))
                .thenReturn(List.of(lesson1, lesson2, lesson3));

        LessonNavigationDTO nav = lessonProgressService.getNavigation(2L, "student@test.com");

        assertEquals(2L, nav.currentLessonId());
        assertEquals(1L, nav.previousLessonId());
        assertEquals(3L, nav.nextLessonId());
        assertTrue(nav.hasPrevious());
        assertTrue(nav.hasNext());
    }

    // ==========================================
    // 4. Lessons Status & Locking Logic
    // ==========================================

    @Test
    @DisplayName("getLessonsStatus locks lesson 2 and 3 when lesson 1 is incomplete")
    void testGetLessonsStatusInitialLocked() {
        when(courseRepository.existsById(100L)).thenReturn(true);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Email("student@test.com")).thenReturn(Optional.of(student));
        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 100L)).thenReturn(Optional.of(enrollment));

        List<Lessons> allLessons = List.of(lesson1, lesson2, lesson3);
        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(100L))
                .thenReturn(allLessons);
        when(lessonProgressRepository.findByStudentAndLessonIn(student, allLessons)).thenReturn(List.of());

        List<LessonStatusDTO> statusList = lessonProgressService.getLessonsStatus(100L, "student@test.com");

        assertEquals(3, statusList.size());
        assertEquals("INCOMPLETE", statusList.get(0).status());
        assertEquals("LOCKED", statusList.get(1).status());
        assertEquals("LOCKED", statusList.get(2).status());
    }

    @Test
    @DisplayName("getLessonsStatus unlocks lesson 2 after lesson 1 is completed")
    void testGetLessonsStatusUnlockedAfterComplete() {
        when(courseRepository.existsById(100L)).thenReturn(true);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Email("student@test.com")).thenReturn(Optional.of(student));
        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 100L)).thenReturn(Optional.of(enrollment));

        List<Lessons> allLessons = List.of(lesson1, lesson2, lesson3);
        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(100L))
                .thenReturn(allLessons);

        LessonProgress lp1 = LessonProgress.builder()
                .student(student)
                .lesson(lesson1)
                .isCompleted(true)
                .build();
        when(lessonProgressRepository.findByStudentAndLessonIn(student, allLessons)).thenReturn(List.of(lp1));

        List<LessonStatusDTO> statusList = lessonProgressService.getLessonsStatus(100L, "student@test.com");

        assertEquals(3, statusList.size());
        assertEquals("COMPLETED", statusList.get(0).status());
        assertEquals("INCOMPLETE", statusList.get(1).status()); // Unlocked!
        assertEquals("LOCKED", statusList.get(2).status());     // Still locked until lesson 2 complete
    }

    // ==========================================
    // 5. Lesson Materials (Attach, Get, Delete)
    // ==========================================

    @Test
    @DisplayName("attachMaterial saves and returns LessonMaterialDTO")
    void testAttachMaterial() {
        Users instructor = Users.builder().id(2L).email("inst@test.com").role(Roles.INSTRUCTOR).build();
        lesson1.setInstructor(instructor);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(userRepository.findByEmail("inst@test.com")).thenReturn(Optional.of(instructor));
        when(lessonMaterialRepository.save(any(LessonMaterial.class))).thenAnswer(inv -> {
            LessonMaterial lm = inv.getArgument(0);
            lm.setMaterialId(50L);
            return lm;
        });

        LessonMaterialCreateDTO createDTO = new LessonMaterialCreateDTO(
                "Slides Part 1",
                "https://res.cloudinary.com/demo/slides.pdf",
                "slides_pub_1",
                "PDF",
                102400L
        );

        LessonMaterialDTO result = lessonMaterialService.attachMaterial(1L, createDTO, "inst@test.com");

        assertNotNull(result);
        assertEquals(50L, result.materialId());
        assertEquals(1L, result.lessonId());
        assertEquals("Slides Part 1", result.title());
        assertEquals("PDF", result.fileType());
    }

    @Test
    @DisplayName("getMaterials returns list of materials for enrolled student")
    void testGetMaterials() {
        mockActiveEnrollment();
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));

        LessonMaterial m = LessonMaterial.builder()
                .materialId(10L)
                .lesson(lesson1)
                .title("Cheat Sheet")
                .fileUrl("https://cloudinary.com/cheatsheet.pdf")
                .fileType("PDF")
                .fileSize(5000L)
                .build();

        when(lessonMaterialRepository.findByLesson_LessonId(1L)).thenReturn(List.of(m));

        List<LessonMaterialDTO> materials = lessonMaterialService.getMaterials(1L, "student@test.com");

        assertEquals(1, materials.size());
        assertEquals("Cheat Sheet", materials.get(0).title());
    }

    @Test
    @DisplayName("deleteMaterial removes material and calls Cloudinary delete")
    void testDeleteMaterial() {
        Users admin = Users.builder().id(99L).email("admin@test.com").role(Roles.ADMIN).build();
        LessonMaterial m = LessonMaterial.builder()
                .materialId(25L)
                .lesson(lesson1)
                .title("Doc")
                .filePublicId("doc_123")
                .build();

        when(lessonMaterialRepository.findById(25L)).thenReturn(Optional.of(m));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        lessonMaterialService.deleteMaterial(25L, "admin@test.com");

        verify(fileUploadService).deleteAsset("doc_123");
        verify(lessonMaterialRepository).delete(m);
    }

    // ==========================================
    // 6. MockMvc Controller Endpoint Tests
    // ==========================================

    @Test
    @DisplayName("MockMvc POST /api/lessons/{id}/complete returns 200 OK")
    void testCompleteEndpoint() throws Exception {
        mockActiveEnrollment();
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonProgressRepository.findByStudentAndLesson(student, lesson1)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> {
            LessonProgress lp = inv.getArgument(0);
            lp.setId(1L);
            return lp;
        });

        Principal mockPrincipal = new UsernamePasswordAuthenticationToken("student@test.com", "pass");

        progressMockMvc.perform(post("/api/lessons/1/complete")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId").value(1))
                .andExpect(jsonPath("$.isCompleted").value(true));
    }

    @Test
    @DisplayName("MockMvc POST /api/lessons/{id}/progress updates seconds")
    void testProgressEndpoint() throws Exception {
        mockActiveEnrollment();
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(lessonProgressRepository.findByStudentAndLesson(student, lesson1)).thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> {
            LessonProgress lp = inv.getArgument(0);
            lp.setId(2L);
            return lp;
        });

        Principal mockPrincipal = new UsernamePasswordAuthenticationToken("student@test.com", "pass");

        progressMockMvc.perform(post("/api/lessons/1/progress?seconds=120.5")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId").value(1))
                .andExpect(jsonPath("$.lastPlaybackPositionSeconds").value(120.5));
    }

    @Test
    @DisplayName("MockMvc GET /api/courses/{id}/lessons-status returns status list")
    void testLessonsStatusEndpoint() throws Exception {
        when(courseRepository.existsById(100L)).thenReturn(true);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Email("student@test.com")).thenReturn(Optional.of(student));
        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 100L)).thenReturn(Optional.of(enrollment));

        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(100L))
                .thenReturn(List.of(lesson1, lesson2));
        when(lessonProgressRepository.findByStudentAndLessonIn(eq(student), any())).thenReturn(List.of());

        Principal mockPrincipal = new UsernamePasswordAuthenticationToken("student@test.com", "pass");

        progressMockMvc.perform(get("/api/courses/100/lessons-status")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("INCOMPLETE"))
                .andExpect(jsonPath("$[1].status").value("LOCKED"));
    }

    @Test
    @DisplayName("Free preview lesson allows getting navigation without enrollment")
    void testFreeLessonNavigationWithoutEnrollment() {
        Lessons freeLesson = new Lessons();
        freeLesson.setLessonId(5L);
        freeLesson.setTitle("Free Preview Video");
        freeLesson.setOrderIndex(0);
        freeLesson.setSection(section);
        freeLesson.setIsFree(true);

        when(lessonRepository.findById(5L)).thenReturn(Optional.of(freeLesson));
        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(100L))
                .thenReturn(List.of(freeLesson));

        LessonNavigationDTO nav = lessonProgressService.getNavigation(5L, null);

        assertNotNull(nav);
        assertEquals(5L, nav.currentLessonId());
    }

    @Test
    @DisplayName("Free preview lesson allows downloading materials without authentication")
    void testFreeLessonMaterialsWithoutAuth() {
        Lessons freeLesson = new Lessons();
        freeLesson.setLessonId(6L);
        freeLesson.setTitle("Free Intro");
        freeLesson.setIsFree(true);
        freeLesson.setSection(section);

        LessonMaterial material = LessonMaterial.builder()
                .materialId(60L)
                .lesson(freeLesson)
                .title("Free Syllabus")
                .fileUrl("https://example.com/syllabus.pdf")
                .fileType("PDF")
                .fileSize(5000L)
                .build();

        when(lessonRepository.findById(6L)).thenReturn(Optional.of(freeLesson));
        when(lessonMaterialRepository.findByLesson_LessonId(6L)).thenReturn(List.of(material));

        List<LessonMaterialDTO> materials = lessonMaterialService.getMaterials(6L, null);

        assertEquals(1, materials.size());
        assertEquals("Free Syllabus", materials.get(0).title());
    }

    @Test
    @DisplayName("getLessonsStatus marks isFree lessons as INCOMPLETE and sets isFree=true")
    void testGetLessonsStatusWithFreeLesson() {
        when(courseRepository.existsById(100L)).thenReturn(true);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Email("student@test.com")).thenReturn(Optional.of(student));
        Enrollments enrollment = new Enrollments();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 100L)).thenReturn(Optional.of(enrollment));

        lesson1.setIsFree(false);
        lesson2.setIsFree(true);
        lesson3.setIsFree(false);

        List<Lessons> allLessons = List.of(lesson1, lesson2, lesson3);
        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(100L))
                .thenReturn(allLessons);
        when(lessonProgressRepository.findByStudentAndLessonIn(student, allLessons)).thenReturn(List.of());

        List<LessonStatusDTO> statusList = lessonProgressService.getLessonsStatus(100L, "student@test.com");

        assertEquals(3, statusList.size());
        assertEquals("INCOMPLETE", statusList.get(0).status());
        assertEquals("INCOMPLETE", statusList.get(1).status());
        assertTrue(statusList.get(1).isFree());
        assertEquals("LOCKED", statusList.get(2).status());
    }
}
