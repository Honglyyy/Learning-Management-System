package com.ly.lmsbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.lmsbackend.controller.AssignmentController;
import com.ly.lmsbackend.dto.assignmentdtos.*;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.AssignmentService;
import com.ly.lmsbackend.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class Stage3AssignmentsAndGradingTests {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentSubmissionRepository submissionRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private FileUploadService fileUploadService;

    private AssignmentService assignmentService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Users studentUser;
    private Users instructorUser;
    private Users adminUser;
    private Courses course;
    private Sections section;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        assignmentService = new AssignmentService(
                assignmentRepository,
                submissionRepository,
                courseRepository,
                sectionRepository,
                userRepository,
                studentRepository,
                enrollmentRepository,
                fileUploadService
        );

        AssignmentController controller = new AssignmentController(assignmentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        studentUser = Users.builder()
                .id(1L)
                .username("student1")
                .email("student@test.com")
                .fullname("Alice Student")
                .role(Roles.STUDENT)
                .build();

        instructorUser = Users.builder()
                .id(2L)
                .username("instructor1")
                .email("instructor@test.com")
                .fullname("Bob Instructor")
                .role(Roles.INSTRUCTOR)
                .build();

        adminUser = Users.builder()
                .id(3L)
                .username("admin1")
                .email("admin@test.com")
                .fullname("Charlie Admin")
                .role(Roles.ADMIN)
                .build();

        course = new Courses();
        course.setCourseId(10L);
        course.setTitle("Full Stack Spring Boot & React");
        course.setInstructor(instructorUser);

        section = new Sections();
        section.setSectionId(100L);
        section.setTitle("Module 1");
        section.setCourse(course);

        assignment = Assignment.builder()
                .assignmentId(50L)
                .course(course)
                .section(section)
                .instructor(instructorUser)
                .title("Build a REST API")
                .description("Create CRUD endpoints for products")
                .instructions("Submit Github repository link and PDF report")
                .startDate(new Timestamp(System.currentTimeMillis() - 86400000L))
                .dueDate(new Timestamp(System.currentTimeMillis() + 86400000L)) // 1 day in future
                .maxScore(100.0)
                .supportingFileUrl("https://cloudinary.com/brief.pdf")
                .supportingFilePublicId("brief_123")
                .allowResubmission(true)
                .build();
    }

    private void mockActiveEnrollment() {
        Enrollments enrollment = new Enrollments();
        enrollment.setUser(studentUser);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 10L)).thenReturn(Optional.of(enrollment));
    }

    // ==========================================
    // 1. Assignment Creation
    // ==========================================

    @Test
    @DisplayName("createAssignment succeeds when course instructor creates assignment")
    void testCreateAssignment_Success() {
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(sectionRepository.findById(100L)).thenReturn(Optional.of(section));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setAssignmentId(51L);
            return a;
        });

        AssignmentCreateDTO dto = new AssignmentCreateDTO(
                10L, 100L, "New Assignment", "Desc", "Instructions",
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis() + 100000L),
                100.0, "url", "pubId", true
        );

        AssignmentResponseDTO result = assignmentService.createAssignment(dto, "instructor@test.com");

        assertNotNull(result);
        assertEquals(51L, result.assignmentId());
        assertEquals("New Assignment", result.title());
        assertEquals(10L, result.courseId());
    }

    @Test
    @DisplayName("createAssignment fails with 403 when non-instructor tries to create assignment")
    void testCreateAssignment_Unauthorized() {
        Users otherUser = Users.builder().id(99L).email("other@test.com").role(Roles.INSTRUCTOR).build();
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        AssignmentCreateDTO dto = new AssignmentCreateDTO(
                10L, null, "New Assignment", "Desc", "Instructions",
                null, null, 100.0, null, null, true
        );

        assertThrows(ResponseStatusException.class, () ->
                assignmentService.createAssignment(dto, "other@test.com")
        );
    }

    // ==========================================
    // 2. Listing & Detail
    // ==========================================

    @Test
    @DisplayName("getCourseAssignments returns assignments for enrolled student")
    void testGetCourseAssignments_EnrolledStudent() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(assignmentRepository.findByCourse_CourseIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(assignment));

        List<AssignmentResponseDTO> list = assignmentService.getCourseAssignments(10L, "student@test.com");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(50L, list.get(0).assignmentId());
    }

    @Test
    @DisplayName("getCourseAssignments throws 403 when student is not enrolled")
    void testGetCourseAssignments_NotEnrolled() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                assignmentService.getCourseAssignments(10L, "student@test.com")
        );
    }

    @Test
    @DisplayName("getAssignmentById returns assignment details")
    void testGetAssignmentById() {
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();

        AssignmentResponseDTO result = assignmentService.getAssignmentById(50L, "student@test.com");

        assertNotNull(result);
        assertEquals("Build a REST API", result.title());
        assertEquals(100.0, result.maxScore());
    }

    // ==========================================
    // 3. Update & Delete
    // ==========================================

    @Test
    @DisplayName("updateAssignment updates fields successfully")
    void testUpdateAssignment() {
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentUpdateDTO updateDTO = new AssignmentUpdateDTO(
                null, "Updated Title", "Updated Desc", "New Instructions",
                null, null, 150.0, null, null, false
        );

        AssignmentResponseDTO updated = assignmentService.updateAssignment(50L, updateDTO, "instructor@test.com");

        assertEquals("Updated Title", updated.title());
        assertEquals(150.0, updated.maxScore());
        assertFalse(updated.allowResubmission());
    }

    @Test
    @DisplayName("deleteAssignment deletes entity and cleans up Cloudinary asset")
    void testDeleteAssignment() {
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));

        assignmentService.deleteAssignment(50L, "instructor@test.com");

        verify(fileUploadService).deleteAsset("brief_123");
        verify(assignmentRepository).delete(assignment);
    }

    // ==========================================
    // 4. Submissions & Late Detection
    // ==========================================

    @Test
    @DisplayName("submitAssignment sets status to SUBMITTED when on time")
    void testSubmitAssignment_OnTime() {
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.empty());
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(inv -> {
            AssignmentSubmission s = inv.getArgument(0);
            s.setSubmissionId(77L);
            return s;
        });

        AssignmentSubmitDTO submitDTO = new AssignmentSubmitDTO("Here is my code", "https://files.com/code.zip", "zip_123");
        AssignmentSubmissionResponseDTO res = assignmentService.submitAssignment(50L, submitDTO, "student@test.com");

        assertNotNull(res);
        assertEquals(77L, res.submissionId());
        assertEquals(AssignmentStatus.SUBMITTED, res.status());
        assertEquals("Here is my code", res.textSubmission());
    }

    @Test
    @DisplayName("submitAssignment sets status to LATE when submitted after due date")
    void testSubmitAssignment_Late() {
        // Due date in the past
        assignment.setDueDate(new Timestamp(System.currentTimeMillis() - 3600000L));

        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.empty());
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(inv -> {
            AssignmentSubmission s = inv.getArgument(0);
            s.setSubmissionId(78L);
            return s;
        });

        AssignmentSubmitDTO submitDTO = new AssignmentSubmitDTO("Late submission", null, null);
        AssignmentSubmissionResponseDTO res = assignmentService.submitAssignment(50L, submitDTO, "student@test.com");

        assertEquals(AssignmentStatus.LATE, res.status());
    }

    @Test
    @DisplayName("submitAssignment allows resubmission when allowResubmission is true")
    void testSubmitAssignment_ResubmissionAllowed() {
        AssignmentSubmission existing = AssignmentSubmission.builder()
                .submissionId(80L)
                .assignment(assignment)
                .student(studentUser)
                .textSubmission("Old draft")
                .status(AssignmentStatus.SUBMITTED)
                .build();

        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.of(existing));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentSubmitDTO submitDTO = new AssignmentSubmitDTO("Revised submission", "https://file.url", "pub_2");
        AssignmentSubmissionResponseDTO res = assignmentService.submitAssignment(50L, submitDTO, "student@test.com");

        assertEquals(80L, res.submissionId());
        assertEquals("Revised submission", res.textSubmission());
    }

    @Test
    @DisplayName("submitAssignment rejects resubmission when allowResubmission is false")
    void testSubmitAssignment_ResubmissionDisallowed() {
        assignment.setAllowResubmission(false);
        AssignmentSubmission existing = AssignmentSubmission.builder()
                .submissionId(80L)
                .assignment(assignment)
                .student(studentUser)
                .textSubmission("Initial submission")
                .status(AssignmentStatus.SUBMITTED)
                .build();

        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.of(existing));

        AssignmentSubmitDTO submitDTO = new AssignmentSubmitDTO("New try", null, null);
        assertThrows(ResponseStatusException.class, () ->
                assignmentService.submitAssignment(50L, submitDTO, "student@test.com")
        );
    }

    @Test
    @DisplayName("submitAssignment rejects edit/resubmission when assignment is already graded")
    void testSubmitAssignment_GradedCannotResubmit() {
        AssignmentSubmission existing = AssignmentSubmission.builder()
                .submissionId(80L)
                .assignment(assignment)
                .student(studentUser)
                .textSubmission("Initial submission")
                .status(AssignmentStatus.GRADED)
                .score(95.0)
                .grade("A")
                .build();

        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.of(existing));

        AssignmentSubmitDTO submitDTO = new AssignmentSubmitDTO("Attempt to edit after grading", null, null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                assignmentService.submitAssignment(50L, submitDTO, "student@test.com")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already been graded"));
    }

    // ==========================================
    // 5. Get My Submission & Status NOT_SUBMITTED
    // ==========================================

    @Test
    @DisplayName("getMySubmission returns NOT_SUBMITTED when student has not yet submitted")
    void testGetMySubmission_NotSubmitted() {
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.empty());

        AssignmentSubmissionResponseDTO res = assignmentService.getMySubmission(50L, "student@test.com");

        assertNotNull(res);
        assertNull(res.submissionId());
        assertEquals(AssignmentStatus.NOT_SUBMITTED, res.status());
    }

    // ==========================================
    // 6. Grading Workflow
    // ==========================================

    @Test
    @DisplayName("gradeSubmission grades submission with score, letter grade, and feedback")
    void testGradeSubmission_Success() {
        AssignmentSubmission sub = AssignmentSubmission.builder()
                .submissionId(90L)
                .assignment(assignment)
                .student(studentUser)
                .textSubmission("Solution code")
                .status(AssignmentStatus.SUBMITTED)
                .build();

        when(submissionRepository.findById(90L)).thenReturn(Optional.of(sub));
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentGradeDTO gradeDTO = new AssignmentGradeDTO(95.0, "A", "Great work!");
        AssignmentSubmissionResponseDTO graded = assignmentService.gradeSubmission(90L, gradeDTO, "instructor@test.com");

        assertNotNull(graded);
        assertEquals(95.0, graded.score());
        assertEquals("A", graded.grade());
        assertEquals("Great work!", graded.feedback());
        assertEquals(AssignmentStatus.GRADED, graded.status());
        assertNotNull(graded.gradedAt());
    }

    @Test
    @DisplayName("gradeSubmission auto-calculates letter grade when not provided")
    void testGradeSubmission_AutoGrade() {
        AssignmentSubmission sub = AssignmentSubmission.builder()
                .submissionId(90L)
                .assignment(assignment)
                .student(studentUser)
                .textSubmission("Solution code")
                .status(AssignmentStatus.SUBMITTED)
                .build();

        when(submissionRepository.findById(90L)).thenReturn(Optional.of(sub));
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        // Score 85 out of 100 -> grade "B"
        AssignmentGradeDTO gradeDTO = new AssignmentGradeDTO(85.0, null, "Good job");
        AssignmentSubmissionResponseDTO graded = assignmentService.gradeSubmission(90L, gradeDTO, "instructor@test.com");

        assertEquals("B", graded.grade());
        assertEquals(AssignmentStatus.GRADED, graded.status());
    }

    // ==========================================
    // 7. MockMvc HTTP Endpoints
    // ==========================================

    @Test
    @DisplayName("MockMvc: POST /api/assignments creates assignment")
    void testMockMvc_CreateAssignment() throws Exception {
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> {
            Assignment a = inv.getArgument(0);
            a.setAssignmentId(55L);
            return a;
        });

        AssignmentCreateDTO dto = new AssignmentCreateDTO(
                10L, null, "REST Assignment", "Desc", "Instructions",
                null, null, 100.0, null, null, true
        );

        Principal principal = new UsernamePasswordAuthenticationToken("instructor@test.com", null);

        mockMvc.perform(post("/api/assignments")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(55L))
                .andExpect(jsonPath("$.title").value("REST Assignment"));
    }

    @Test
    @DisplayName("MockMvc: GET /api/courses/{id}/assignments returns list")
    void testMockMvc_GetCourseAssignments() throws Exception {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(assignmentRepository.findByCourse_CourseIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(assignment));

        Principal principal = new UsernamePasswordAuthenticationToken("student@test.com", null);

        mockMvc.perform(get("/api/courses/10/assignments").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assignmentId").value(50L))
                .andExpect(jsonPath("$[0].title").value("Build a REST API"));
    }

    @Test
    @DisplayName("MockMvc: POST /api/assignments/{id}/submit submits work")
    void testMockMvc_SubmitAssignment() throws Exception {
        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.empty());
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(inv -> {
            AssignmentSubmission s = inv.getArgument(0);
            s.setSubmissionId(101L);
            return s;
        });

        AssignmentSubmitDTO dto = new AssignmentSubmitDTO("My Homework", "https://cloud.com/hw.pdf", "hw_1");
        Principal principal = new UsernamePasswordAuthenticationToken("student@test.com", null);

        mockMvc.perform(post("/api/assignments/50/submit")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submissionId").value(101L))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("MockMvc: PUT /api/assignments/submissions/{id}/grade grades work")
    void testMockMvc_GradeSubmission() throws Exception {
        AssignmentSubmission sub = AssignmentSubmission.builder()
                .submissionId(101L)
                .assignment(assignment)
                .student(studentUser)
                .textSubmission("Code")
                .status(AssignmentStatus.SUBMITTED)
                .build();

        when(submissionRepository.findById(101L)).thenReturn(Optional.of(sub));
        when(userRepository.findByEmail("instructor@test.com")).thenReturn(Optional.of(instructorUser));
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentGradeDTO dto = new AssignmentGradeDTO(95.0, "A", "Great work!");
        Principal principal = new UsernamePasswordAuthenticationToken("instructor@test.com", null);

        mockMvc.perform(put("/api/assignments/submissions/101/grade")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(95.0))
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.feedback").value("Great work!"))
                .andExpect(jsonPath("$.status").value("GRADED"));
    }

    @Test
    @DisplayName("MockMvc: GET /api/assignments/{id}/submission/me returns my submission")
    void testMockMvc_GetMySubmission() throws Exception {
        AssignmentSubmission sub = AssignmentSubmission.builder()
                .submissionId(101L)
                .assignment(assignment)
                .student(studentUser)
                .textSubmission("Code")
                .score(95.0)
                .grade("A")
                .feedback("Great work!")
                .status(AssignmentStatus.GRADED)
                .build();

        when(assignmentRepository.findById(50L)).thenReturn(Optional.of(assignment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        mockActiveEnrollment();
        when(submissionRepository.findByAssignment_AssignmentIdAndStudent_Id(50L, 1L)).thenReturn(Optional.of(sub));

        Principal principal = new UsernamePasswordAuthenticationToken("student@test.com", null);

        mockMvc.perform(get("/api/assignments/50/submission/me").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(95.0))
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.feedback").value("Great work!"))
                .andExpect(jsonPath("$.status").value("GRADED"));
    }
}
