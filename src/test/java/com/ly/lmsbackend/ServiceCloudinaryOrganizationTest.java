package com.ly.lmsbackend;

import com.ly.lmsbackend.dto.assignmentdtos.AssignmentCreateDTO;
import com.ly.lmsbackend.dto.assignmentdtos.AssignmentSubmitDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseCreateDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonCreateDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonMaterialCreateDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonMaterialDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonResponseDTO;
import com.ly.lmsbackend.mapper.CourseMapper;
import com.ly.lmsbackend.mapper.LessonMapper;
import com.ly.lmsbackend.mapper.SectionMapper;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceCloudinaryOrganizationTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SectionMapper sectionMapper;
    @Mock
    private CourseReviewRepository courseReviewRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private CourseFavoriteRepository courseFavoriteRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private FileUploadService fileUploadService;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonMapper lessonMapper;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private LessonProgressRepository lessonProgressRepository;
    @Mock
    private LessonMaterialRepository lessonMaterialRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private AssignmentSubmissionRepository submissionRepository;

    private CourseService courseService;
    private LessonService lessonService;
    private LessonMaterialService lessonMaterialService;
    private AssignmentService assignmentService;

    private Users instructorUser;
    private Users studentUser;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(
                courseRepository, courseMapper, categoryRepository, userRepository,
                sectionRepository, sectionMapper, courseReviewRepository, quizAttemptRepository,
                courseFavoriteRepository, enrollmentRepository, fileUploadService
        );

        lessonService = new LessonService(
                lessonMapper, lessonRepository, sectionRepository, courseRepository,
                quizRepository, quizAttemptRepository, lessonProgressRepository, userRepository,
                enrollmentRepository, fileUploadService
        );

        lessonMaterialService = new LessonMaterialService(
                lessonMaterialRepository, lessonRepository, studentRepository,
                enrollmentRepository, userRepository, fileUploadService
        );

        assignmentService = new AssignmentService(
                assignmentRepository, submissionRepository, courseRepository, sectionRepository,
                userRepository, studentRepository, enrollmentRepository, fileUploadService
        );

        instructorUser = Users.builder()
                .id(1L)
                .username("john_instructor")
                .email("john@example.com")
                .role(Roles.INSTRUCTOR)
                .build();

        studentUser = Users.builder()
                .id(2L)
                .username("jane_student")
                .email("jane@example.com")
                .role(Roles.STUDENT)
                .build();
    }

    @Test
    @DisplayName("CourseService organizes course cover on create")
    void testCourseServiceOrganizesCoverOnCreate() {
        CourseCreateDTO dto = new CourseCreateDTO(
                "Advanced Spring Boot",
                "Learn Spring",
                BigDecimal.valueOf(99),
                "10h",
                "https://res.cloudinary.com/test/course/john_instructor/general/cover123.jpg",
                "course/john_instructor/general/cover123",
                instructorUser.getId(),
                instructorUser.getUsername(),
                List.of(1L),
                CourseLevel.ADVANCED,
                CourseStatus.DRAFT,
                "Microservices",
                "Basic Java"
        );

        Courses entity = new Courses();
        entity.setCourseId(10L);
        entity.setTitle(dto.title());
        entity.setInstructor(instructorUser);
        entity.setCoverUrl(dto.coverUrl());
        entity.setCoverPublicId(dto.coverPublicId());

        when(userRepository.findById(instructorUser.getId())).thenReturn(Optional.of(instructorUser));
        when(categoryRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(courseMapper.toEntity(eq(dto), eq(instructorUser), any())).thenReturn(entity);
        when(fileUploadService.organizeCourseCover(
                "course/john_instructor/general/cover123",
                "john_instructor",
                "Advanced Spring Boot"
        )).thenReturn(new FileUploadService.OrganizedAsset(
                "course/john_instructor/advanced-spring-boot/cover123",
                "https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/cover123.jpg"
        ));
        when(courseRepository.save(any(Courses.class))).thenAnswer(i -> i.getArgument(0));
        when(courseMapper.toDTO(any(Courses.class))).thenReturn(mock(CourseResponseDTO.class));

        courseService.addCourse(dto);

        assertEquals("course/john_instructor/advanced-spring-boot/cover123", entity.getCoverPublicId());
        assertEquals("https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/cover123.jpg", entity.getCoverUrl());
        verify(fileUploadService).organizeCourseCover(
                "course/john_instructor/general/cover123",
                "john_instructor",
                "Advanced Spring Boot"
        );
    }

    @Test
    @DisplayName("CourseService deletes cover asset when course is deleted")
    void testCourseServiceDeletesCoverOnDelete() {
        Courses course = new Courses();
        course.setCourseId(10L);
        course.setTitle("Advanced Spring Boot");
        course.setCoverPublicId("course/john_instructor/advanced-spring-boot/cover123");
        course.setCategories(new ArrayList<>());

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(10L);

        verify(fileUploadService).deleteAsset("course/john_instructor/advanced-spring-boot/cover123");
        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("LessonService organizes lesson video on addLesson")
    void testLessonServiceOrganizesVideoOnAddLesson() {
        Courses course = new Courses();
        course.setCourseId(10L);
        course.setTitle("Advanced Spring Boot");
        course.setInstructor(instructorUser);

        Sections section = new Sections();
        section.setSectionId(5L);
        section.setTitle("Section 1");
        section.setCourse(course);
        section.setInstructor(instructorUser);

        LessonCreateDTO dto = new LessonCreateDTO(
                "Introduction to Reactive Streams",
                "https://res.cloudinary.com/test/course/john_instructor/general/video123.mp4",
                "course/john_instructor/general/video123",
                5L,
                "Desc",
                "Content",
                1,
                "15m",
                false
        );

        Lessons entity = new Lessons();
        entity.setLessonId(20L);
        entity.setTitle(dto.title());
        entity.setSection(section);
        entity.setVideoUrl(dto.videoUrl());
        entity.setVideoPublicId(dto.videoPublicId());

        when(sectionRepository.findById(5L)).thenReturn(Optional.of(section));
        when(lessonMapper.toEntity(dto, section)).thenReturn(entity);
        when(fileUploadService.organizeLessonVideo(
                "course/john_instructor/general/video123",
                "john_instructor",
                "Advanced Spring Boot",
                "Introduction to Reactive Streams"
        )).thenReturn(new FileUploadService.OrganizedAsset(
                "course/john_instructor/advanced-spring-boot/introduction-to-reactive-streams/video123",
                "https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/introduction-to-reactive-streams/video123.mp4"
        ));
        when(lessonRepository.save(any(Lessons.class))).thenAnswer(i -> i.getArgument(0));
        when(lessonMapper.toDto(any(Lessons.class))).thenReturn(mock(LessonResponseDTO.class));

        lessonService.addLesson(dto);

        assertEquals("course/john_instructor/advanced-spring-boot/introduction-to-reactive-streams/video123", entity.getVideoPublicId());
        verify(fileUploadService).organizeLessonVideo(
                "course/john_instructor/general/video123",
                "john_instructor",
                "Advanced Spring Boot",
                "Introduction to Reactive Streams"
        );
    }

    @Test
    @DisplayName("LessonMaterialService organizes material asset on attach")
    void testLessonMaterialServiceOrganizesMaterial() {
        Courses course = new Courses();
        course.setCourseId(10L);
        course.setTitle("Advanced Spring Boot");
        course.setInstructor(instructorUser);

        Sections section = new Sections();
        section.setSectionId(5L);
        section.setCourse(course);

        Lessons lesson = new Lessons();
        lesson.setLessonId(20L);
        lesson.setTitle("Introduction to Reactive Streams");
        lesson.setSection(section);
        lesson.setInstructor(instructorUser);

        LessonMaterialCreateDTO dto = new LessonMaterialCreateDTO(
                "Reactive Cheatsheet",
                "https://res.cloudinary.com/test/course/john_instructor/general/cheat.pdf",
                "course/john_instructor/general/cheat",
                "PDF",
                1024L
        );

        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(instructorUser));
        when(fileUploadService.organizeMaterial(
                "course/john_instructor/general/cheat",
                "john_instructor",
                "Advanced Spring Boot",
                "Introduction to Reactive Streams"
        )).thenReturn(new FileUploadService.OrganizedAsset(
                "course/john_instructor/advanced-spring-boot/introduction-to-reactive-streams/cheat",
                "https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/introduction-to-reactive-streams/cheat.pdf"
        ));
        when(lessonMaterialRepository.save(any(LessonMaterial.class))).thenAnswer(i -> i.getArgument(0));

        LessonMaterialDTO result = lessonMaterialService.attachMaterial(20L, dto, "john@example.com");

        assertNotNull(result);
        assertEquals("https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/introduction-to-reactive-streams/cheat.pdf", result.fileUrl());
    }

    @Test
    @DisplayName("AssignmentService organizes supporting file on create and submission file on submit")
    void testAssignmentServiceOrganizesFiles() {
        Courses course = new Courses();
        course.setCourseId(10L);
        course.setTitle("Advanced Spring Boot");
        course.setInstructor(instructorUser);

        AssignmentCreateDTO createDTO = new AssignmentCreateDTO(
                10L,
                null,
                "Homework 1",
                "Description",
                "Instructions",
                null,
                null,
                100.0,
                "https://res.cloudinary.com/test/course/john_instructor/general/hw1.pdf",
                "course/john_instructor/general/hw1",
                true
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(instructorUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(fileUploadService.organizeAssignmentSupportingFile(
                "course/john_instructor/general/hw1",
                "john_instructor",
                "Advanced Spring Boot",
                null
        )).thenReturn(new FileUploadService.OrganizedAsset(
                "course/john_instructor/advanced-spring-boot/assignments/hw1",
                "https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/assignments/hw1.pdf"
        ));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> {
            Assignment a = i.getArgument(0);
            a.setAssignmentId(50L);
            return a;
        });

        var createdResponse = assignmentService.createAssignment(createDTO, "john@example.com");
        assertNotNull(createdResponse);
        assertEquals("course/john_instructor/advanced-spring-boot/assignments/hw1", createdResponse.supportingFilePublicId());

        // Now test student submission
        Assignment assignment = Assignment.builder()
                .assignmentId(50L)
                .course(course)
                .instructor(instructorUser)
                .title("Homework 1")
                .allowResubmission(true)
                .build();

        Enrollments enrollment = new Enrollments();
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(studentUser));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(studentUser.getId(), 10L)).thenReturn(Optional.of(enrollment));
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, studentUser.getId())).thenReturn(Optional.empty());

        when(fileUploadService.organizeAssignmentSubmission(
                "course/jane_student/general/sub1",
                "john_instructor",
                "Advanced Spring Boot",
                "jane_student"
        )).thenReturn(new FileUploadService.OrganizedAsset(
                "course/john_instructor/advanced-spring-boot/assignments/submissions/jane_student/sub1",
                "https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/assignments/submissions/jane_student/sub1.zip"
        ));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(i -> i.getArgument(0));

        AssignmentSubmitDTO submitDTO = new AssignmentSubmitDTO(
                "Here is my solution",
                "https://res.cloudinary.com/test/course/jane_student/general/sub1.zip",
                "course/jane_student/general/sub1"
        );

        var submissionResponse = assignmentService.submitAssignment(50L, submitDTO, "jane@example.com");
        assertNotNull(submissionResponse);
        assertEquals("course/john_instructor/advanced-spring-boot/assignments/submissions/jane_student/sub1", submissionResponse.filePublicId());
        assertEquals("https://res.cloudinary.com/test/course/john_instructor/advanced-spring-boot/assignments/submissions/jane_student/sub1.zip", submissionResponse.fileUrl());
    }
}
