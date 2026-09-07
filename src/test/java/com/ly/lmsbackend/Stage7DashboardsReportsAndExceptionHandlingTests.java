package com.ly.lmsbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ly.lmsbackend.controller.DashboardController;
import com.ly.lmsbackend.controller.ReportController;
import com.ly.lmsbackend.dto.dashboarddtos.AdminDashboardDTO;
import com.ly.lmsbackend.dto.dashboarddtos.InstructorDashboardDTO;
import com.ly.lmsbackend.dto.dashboarddtos.StudentDashboardDTO;
import com.ly.lmsbackend.dto.progressdtos.ContinueLearningDTO;
import com.ly.lmsbackend.dto.reportdtos.EnrollmentReportDTO;
import com.ly.lmsbackend.dto.reportdtos.ProgressReportDTO;
import com.ly.lmsbackend.dto.reportdtos.QuizReportDTO;
import com.ly.lmsbackend.exception.GlobalExceptionHandler;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.DashboardService;
import com.ly.lmsbackend.service.ProgressService;
import com.ly.lmsbackend.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class Stage7DashboardsReportsAndExceptionHandlingTests {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentSubmissionRepository assignmentSubmissionRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private CourseReviewRepository courseReviewRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ProgressService progressService;

    private DashboardService dashboardService;
    private ReportService reportService;

    private DashboardController dashboardController;
    private ReportController reportController;

    private MockMvc dashboardMockMvc;
    private MockMvc reportMockMvc;
    private MockMvc exceptionMockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Dummy controller to verify validation and global exception handler
    public record TestValidationDTO(@NotBlank(message = "Title is required") String title, @NotNull(message = "Count is required") Integer count) {}

    @RestController
    static class TestExceptionController {
        @PostMapping("/test/validation")
        public String testValidation(@Valid @RequestBody TestValidationDTO dto) {
            return "OK";
        }

        @GetMapping("/test/response-status")
        public String testResponseStatus() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target resource was not found");
        }

        @GetMapping("/test/access-denied")
        public String testAccessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/test/bad-credentials")
        public String testBadCredentials() {
            throw new BadCredentialsException("Bad credentials provided");
        }

        @GetMapping("/test/bad-request")
        public String testBadRequest() {
            throw new IllegalArgumentException("Illegal argument given");
        }

        @GetMapping("/test/internal-error")
        public String testInternalError() {
            throw new RuntimeException("Unexpected database failure");
        }
    }

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                userRepository,
                studentRepository,
                instructorRepository,
                courseRepository,
                enrollmentRepository,
                assignmentRepository,
                assignmentSubmissionRepository,
                quizRepository,
                quizAttemptRepository,
                courseReviewRepository,
                paymentRepository,
                progressService
        );

        reportService = new ReportService(
                userRepository,
                courseRepository,
                enrollmentRepository,
                assignmentRepository,
                assignmentSubmissionRepository,
                quizRepository,
                quizAttemptRepository
        );

        dashboardController = new DashboardController(dashboardService);
        reportController = new ReportController(reportService);

        GlobalExceptionHandler advice = new GlobalExceptionHandler();

        dashboardMockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setControllerAdvice(advice)
                .build();

        reportMockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setControllerAdvice(advice)
                .build();

        exceptionMockMvc = MockMvcBuilders.standaloneSetup(new TestExceptionController())
                .setControllerAdvice(advice)
                .build();
    }

    // ==========================================
    // 1. Global Exception Handling Tests
    // ==========================================

    @Test
    @DisplayName("GlobalExceptionHandler: MethodArgumentNotValidException returns HTTP 400 with fieldErrors")
    void testValidationExceptionHandling() throws Exception {
        TestValidationDTO invalidDto = new TestValidationDTO("", null);

        exceptionMockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"))
                .andExpect(jsonPath("$.fieldErrors.count").value("Count is required"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler: ResponseStatusException returns proper HTTP status and message")
    void testResponseStatusExceptionHandling() throws Exception {
        exceptionMockMvc.perform(get("/test/response-status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Target resource was not found"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler: AccessDeniedException returns HTTP 403 Forbidden")
    void testAccessDeniedExceptionHandling() throws Exception {
        exceptionMockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler: BadCredentialsException returns HTTP 401 Unauthorized")
    void testBadCredentialsExceptionHandling() throws Exception {
        exceptionMockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler: IllegalArgumentException returns HTTP 400 Bad Request")
    void testBadRequestExceptionHandling() throws Exception {
        exceptionMockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Illegal argument given"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler: Unhandled Exception returns HTTP 500 Internal Server Error")
    void testGeneralExceptionHandling() throws Exception {
        exceptionMockMvc.perform(get("/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected database failure"));
    }

    // ==========================================
    // 2. Student Dashboard Tests
    // ==========================================

    @Test
    @DisplayName("Dashboard: Student Dashboard returns correct metrics")
    void testStudentDashboard() throws Exception {
        String email = "student@test.com";
        Users studentUser = Users.builder().id(10L).email(email).username("student1").role(Roles.STUDENT).build();
        Students studentProfile = Students.builder().id(1L).user(studentUser).totalPoints(150.0).build();

        Courses course1 = new Courses();
        course1.setCourseId(101L);
        course1.setTitle("Spring Boot Basics");

        Courses course2 = new Courses();
        course2.setCourseId(102L);
        course2.setTitle("React Advanced");

        Enrollments e1 = new Enrollments();
        e1.setEnrollmentId(1L);
        e1.setUser(studentUser);
        e1.setCourse(course1);
        e1.setStatus(EnrollmentStatus.COMPLETED);
        e1.setEarnedPoints(100.0);

        Enrollments e2 = new Enrollments();
        e2.setEnrollmentId(2L);
        e2.setUser(studentUser);
        e2.setCourse(course2);
        e2.setStatus(EnrollmentStatus.ACTIVE);
        e2.setEarnedPoints(50.0);

        QuizAttempt attempt1 = new QuizAttempt();
        attempt1.setEarnedPoints(80.0);
        attempt1.setTotalPoints(100.0);

        QuizAttempt attempt2 = new QuizAttempt();
        attempt2.setEarnedPoints(90.0);
        attempt2.setTotalPoints(100.0);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByUser_Email(email)).thenReturn(Optional.of(studentProfile));
        when(enrollmentRepository.findByUser_Email(email)).thenReturn(List.of(e1, e2));
        when(quizAttemptRepository.findByUser_Email(email)).thenReturn(List.of(attempt1, attempt2));

        ContinueLearningDTO continueDTO = ContinueLearningDTO.builder()
                .courseId(102L)
                .courseTitle("React Advanced")
                .nextLessonId(5L)
                .nextLessonTitle("Hooks Overview")
                .progressPercentage(50.0)
                .build();
        when(progressService.getContinueLearning(email)).thenReturn(continueDTO);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, studentUser.getAuthorities());

        dashboardMockMvc.perform(get("/api/dashboard/student").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEnrolledCourses").value(2))
                .andExpect(jsonPath("$.inProgressCourses").value(1))
                .andExpect(jsonPath("$.completedCourses").value(1))
                .andExpect(jsonPath("$.averageQuizScore").value(85.0))
                .andExpect(jsonPath("$.totalEarnedPoints").value(150.0))
                .andExpect(jsonPath("$.continueLearning.courseTitle").value("React Advanced"));
    }

    // ==========================================
    // 3. Instructor Dashboard Tests
    // ==========================================

    @Test
    @DisplayName("Dashboard: Instructor Dashboard returns correct metrics")
    void testInstructorDashboard() throws Exception {
        String email = "instructor@test.com";
        Users instUser = Users.builder().id(20L).email(email).username("prof_john").role(Roles.INSTRUCTOR).build();

        Courses c1 = new Courses();
        c1.setCourseId(201L);
        c1.setTitle("Java Fundamentals");
        c1.setInstructor(instUser);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(instUser));
        when(courseRepository.findByInstructor_Email(email)).thenReturn(List.of(c1));

        Users studentA = Users.builder().id(101L).build();
        Enrollments e1 = new Enrollments();
        e1.setUser(studentA);
        e1.setCourse(c1);
        e1.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.findByCourse_CourseIdIn(List.of(201L))).thenReturn(List.of(e1));
        when(assignmentSubmissionRepository.countByAssignment_Course_CourseIdInAndStatusIn(
                eq(List.of(201L)), any()))
                .thenReturn(3L);

        CourseReviews review = new CourseReviews();
        review.setRating(4);
        when(courseReviewRepository.findByCourse_CourseIdIn(List.of(201L))).thenReturn(List.of(review));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, instUser.getAuthorities());

        dashboardMockMvc.perform(get("/api/dashboard/instructor").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCoursesCreated").value(1))
                .andExpect(jsonPath("$.activeStudentCount").value(1))
                .andExpect(jsonPath("$.totalEnrollments").value(1))
                .andExpect(jsonPath("$.pendingSubmissionsToGrade").value(3))
                .andExpect(jsonPath("$.averageCourseRating").value(4.0));
    }

    // ==========================================
    // 4. Admin Dashboard Tests
    // ==========================================

    @Test
    @DisplayName("Dashboard: Admin Dashboard returns platform-wide metrics")
    void testAdminDashboard() throws Exception {
        when(studentRepository.count()).thenReturn(50L);
        when(userRepository.countByRoleIn(any())).thenReturn(50L);
        when(instructorRepository.count()).thenReturn(5L);
        when(userRepository.countByRole(Roles.INSTRUCTOR)).thenReturn(5L);
        when(courseRepository.count()).thenReturn(10L);
        when(enrollmentRepository.count()).thenReturn(100L);
        when(enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED)).thenReturn(40L);
        when(paymentRepository.sumAmountByStatus(PaymentStatus.PAID)).thenReturn(new BigDecimal("2500.00"));
        when(quizRepository.count()).thenReturn(20L);
        when(assignmentRepository.count()).thenReturn(15L);

        dashboardMockMvc.perform(get("/api/dashboard/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(50))
                .andExpect(jsonPath("$.totalInstructors").value(5))
                .andExpect(jsonPath("$.totalCourses").value(10))
                .andExpect(jsonPath("$.totalEnrollments").value(100))
                .andExpect(jsonPath("$.completedEnrollments").value(40))
                .andExpect(jsonPath("$.completionRate").value(40.0))
                .andExpect(jsonPath("$.grossRevenue").value(2500.00))
                .andExpect(jsonPath("$.totalQuizzes").value(20))
                .andExpect(jsonPath("$.totalAssignments").value(15));
    }

    // ==========================================
    // 5. Reports Tests
    // ==========================================

    @Test
    @DisplayName("Reports: Enrollment Report returns monthly & course breakdown")
    void testEnrollmentReport() throws Exception {
        String adminEmail = "admin@test.com";
        Users adminUser = Users.builder().id(1L).email(adminEmail).role(Roles.ADMIN).build();
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

        Courses course = new Courses();
        course.setCourseId(1L);
        course.setTitle("Docker Essentials");
        course.setInstructor(adminUser);
        when(courseRepository.findAll()).thenReturn(List.of(course));

        Enrollments e = new Enrollments();
        e.setCourse(course);
        e.setStatus(EnrollmentStatus.ACTIVE);
        e.setEnrolledAt(Timestamp.from(Instant.parse("2026-08-15T10:00:00Z")));
        when(enrollmentRepository.findAll()).thenReturn(List.of(e));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(adminEmail, null, adminUser.getAuthorities());

        reportMockMvc.perform(get("/api/reports/enrollments").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEnrollments").value(1))
                .andExpect(jsonPath("$.monthlyBreakdown[0].month").value("2026-08"))
                .andExpect(jsonPath("$.monthlyBreakdown[0].count").value(1))
                .andExpect(jsonPath("$.courseBreakdown[0].courseTitle").value("Docker Essentials"))
                .andExpect(jsonPath("$.courseBreakdown[0].enrollmentCount").value(1));
    }

    @Test
    @DisplayName("Reports: Progress Report returns completion and submission rates")
    void testProgressReport() throws Exception {
        String adminEmail = "admin@test.com";
        Users adminUser = Users.builder().id(1L).email(adminEmail).role(Roles.ADMIN).build();
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

        Courses course = new Courses();
        course.setCourseId(1L);
        course.setTitle("Kubernetes Pro");
        when(courseRepository.findAll()).thenReturn(List.of(course));

        Enrollments e = new Enrollments();
        e.setCourse(course);
        e.setStatus(EnrollmentStatus.COMPLETED);
        when(enrollmentRepository.findByCourse_CourseId(1L)).thenReturn(List.of(e));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setEarnedPoints(10.0);
        attempt.setTotalPoints(10.0);
        when(quizAttemptRepository.findByEnrollment_Course_CourseId(1L)).thenReturn(List.of(attempt));

        Assignment assignment = Assignment.builder().assignmentId(1L).course(course).build();
        when(assignmentRepository.findByCourse_CourseId(1L)).thenReturn(List.of(assignment));

        AssignmentSubmission submission = AssignmentSubmission.builder()
                .assignment(assignment)
                .status(AssignmentStatus.GRADED)
                .build();
        when(assignmentSubmissionRepository.findByAssignment_Course_CourseId(1L)).thenReturn(List.of(submission));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(adminEmail, null, adminUser.getAuthorities());

        reportMockMvc.perform(get("/api/reports/progress").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudentsEnrolled").value(1))
                .andExpect(jsonPath("$.overallCompletionRate").value(100.0))
                .andExpect(jsonPath("$.overallAverageQuizScore").value(100.0))
                .andExpect(jsonPath("$.courseProgressList[0].courseTitle").value("Kubernetes Pro"))
                .andExpect(jsonPath("$.courseProgressList[0].gradedSubmissions").value(1));
    }

    @Test
    @DisplayName("Reports: Quiz Report returns pass rates and hardest questions")
    void testQuizReport() throws Exception {
        String adminEmail = "admin@test.com";
        Users adminUser = Users.builder().id(1L).email(adminEmail).role(Roles.ADMIN).build();
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

        Quizzes quiz = new Quizzes();
        quiz.setQuizId(1L);
        quiz.setQuizTitle("General Knowledge");

        Questions q1 = new Questions();
        q1.setQuestionId(10L);
        q1.setQuestionText("What is Java?");
        quiz.setQuestions(List.of(q1));

        when(quizRepository.findAll()).thenReturn(List.of(quiz));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(adminUser);
        attempt.setTotalQuestions(1);
        attempt.setCorrectAnswers(1);
        attempt.setEarnedPoints(10.0);
        attempt.setTotalPoints(10.0);
        when(quizAttemptRepository.findByQuiz_QuizId(1L)).thenReturn(List.of(attempt));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(adminEmail, null, adminUser.getAuthorities());

        reportMockMvc.perform(get("/api/reports/quizzes").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuizzes").value(1))
                .andExpect(jsonPath("$.totalAttempts").value(1))
                .andExpect(jsonPath("$.overallPassRate").value(100.0))
                .andExpect(jsonPath("$.overallAverageScore").value(100.0))
                .andExpect(jsonPath("$.quizSummaries[0].quizTitle").value("General Knowledge"))
                .andExpect(jsonPath("$.hardestQuestions[0].questionText").value("What is Java?"));
    }
}
