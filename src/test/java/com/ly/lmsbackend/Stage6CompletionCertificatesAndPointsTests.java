package com.ly.lmsbackend;

import com.ly.lmsbackend.controller.*;
import com.ly.lmsbackend.dto.certificatedtos.CertificateResponseDTO;
import com.ly.lmsbackend.dto.certificatedtos.CertificateVerifyDTO;
import com.ly.lmsbackend.dto.progressdtos.ContinueLearningDTO;
import com.ly.lmsbackend.dto.progressdtos.CourseProgressDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.*;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class Stage6CompletionCertificatesAndPointsTests {

    @Mock private CertificateRepository certificateRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private ActivityLogRepository activityLogRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentSubmissionRepository submissionRepository;
    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;

    private StudentPointsService studentPointsService;
    private NotificationService notificationService;
    private ActivityLogService activityLogService;
    private ProgressService progressService;
    private CertificateService certificateService;

    private ProgressController progressController;
    private CertificateController certificateController;
    private NotificationController notificationController;
    private ActivityLogController activityLogController;

    private MockMvc mockMvcProgress;
    private MockMvc mockMvcCertificate;
    private MockMvc mockMvcNotification;
    private MockMvc mockMvcActivityLog;

    private Users studentUser;
    private Students studentProfile;
    private Courses course;
    private Enrollments enrollment;

    @BeforeEach
    void setUp() {
        studentPointsService = new StudentPointsService(
                quizAttemptRepository,
                submissionRepository,
                quizRepository,
                assignmentRepository,
                enrollmentRepository,
                studentRepository
        );

        notificationService = new NotificationService(notificationRepository, userRepository);
        activityLogService = new ActivityLogService(activityLogRepository);

        progressService = new ProgressService(
                courseRepository,
                enrollmentRepository,
                lessonRepository,
                lessonProgressRepository,
                quizRepository,
                quizAttemptRepository,
                assignmentRepository,
                submissionRepository,
                userRepository,
                studentRepository,
                studentPointsService,
                notificationService,
                activityLogService
        );

        certificateService = new CertificateService(
                certificateRepository,
                courseRepository,
                enrollmentRepository,
                userRepository,
                progressService,
                notificationService,
                activityLogService
        );

        progressController = new ProgressController(progressService);
        certificateController = new CertificateController(certificateService);
        notificationController = new NotificationController(notificationService);
        activityLogController = new ActivityLogController(activityLogService);

        mockMvcProgress = MockMvcBuilders.standaloneSetup(progressController).build();
        mockMvcCertificate = MockMvcBuilders.standaloneSetup(certificateController).build();
        mockMvcNotification = MockMvcBuilders.standaloneSetup(notificationController).build();
        mockMvcActivityLog = MockMvcBuilders.standaloneSetup(activityLogController).build();

        studentUser = Users.builder()
                .id(1L)
                .username("annasmartinez")
                .email("anna@example.com")
                .fullname("Anna S. Martinez")
                .role(Roles.STUDENT)
                .isVerified(true)
                .build();

        studentProfile = Students.builder()
                .id(1L)
                .user(studentUser)
                .studentCode("STU-2026-0001")
                .fullName("Anna S. Martinez")
                .totalPoints(0.0)
                .build();
        studentUser.setStudent(studentProfile);

        course = new Courses();
        course.setCourseId(10L);
        course.setTitle("Plumbing Journeyman Readiness Program");
        course.setDescription("Hands-on competency in DWV layout, water distribution, and inspection compliance.");
        course.setOverallDuration("16 Weeks (240 Hours)");

        enrollment = new Enrollments();
        enrollment.setEnrollmentId(100L);
        enrollment.setUser(studentUser);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEarnedPoints(0.0);
        enrollment.setTotalPoints(0.0);
        enrollment.setEnrolledAt(new Timestamp(System.currentTimeMillis()));
    }

    // ==========================================
    // 1. Points Accumulation Tests
    // ==========================================

    @Test
    @DisplayName("Points from quizzes and assignments add together and store in Enrollment and Student")
    void testRecalculateCourseAndStudentPoints_AddsQuizAndAssignmentPoints() {
        // Prepare Quiz
        Quizzes quiz1 = new Quizzes();
        quiz1.setQuizId(1L);
        quiz1.setTotalPoint(20.0);
        Sections s = new Sections();
        s.setCourse(course);
        Lessons l = new Lessons();
        l.setLessonId(101L);
        l.setSection(s);
        quiz1.setLesson(l);

        QuizAttempt attempt1 = new QuizAttempt();
        attempt1.setAttemptId(501L);
        attempt1.setUser(studentUser);
        attempt1.setQuiz(quiz1);
        attempt1.setEarnedPoints(18.0);
        attempt1.setTotalPoints(20.0);

        // Prepare Assignment
        Assignment assignment1 = Assignment.builder()
                .assignmentId(2L)
                .course(course)
                .maxScore(80.0)
                .build();

        AssignmentSubmission submission1 = AssignmentSubmission.builder()
                .submissionId(601L)
                .assignment(assignment1)
                .student(studentUser)
                .score(75.0)
                .status(AssignmentStatus.GRADED)
                .build();

        when(quizAttemptRepository.findAll()).thenReturn(List.of(attempt1));
        when(quizRepository.findAll()).thenReturn(List.of(quiz1));
        when(submissionRepository.findByStudent_Id(studentUser.getId())).thenReturn(List.of(submission1));
        when(assignmentRepository.findByCourse_CourseIdOrderByCreatedAtDesc(course.getCourseId())).thenReturn(List.of(assignment1));
        when(studentRepository.findByUser_Id(studentUser.getId())).thenReturn(Optional.of(studentProfile));

        // Act
        studentPointsService.recalculateCourseAndStudentPoints(studentUser, course, enrollment);

        // Assert: 18.0 (quiz) + 75.0 (assignment) = 93.0 earned points
        assertEquals(93.0, enrollment.getEarnedPoints());
        // Total possible points: 20.0 (quiz) + 80.0 (assignment) = 100.0
        assertEquals(100.0, enrollment.getTotalPoints());
        // Global student profile total points should be 93.0
        assertEquals(93.0, studentProfile.getTotalPoints());

        verify(enrollmentRepository).save(enrollment);
        verify(studentRepository).save(studentProfile);
    }

    // ==========================================
    // 2. Course Progress & Auto Completion Tests
    // ==========================================

    @Test
    @DisplayName("Course progress reaches 100% when all lessons, quizzes, and assignments are completed")
    void testCourseProgress_Reaches100AndCompletesCourse() {
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(course.getCourseId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(studentUser.getId(), course.getCourseId()))
                .thenReturn(Optional.of(enrollment));

        // 2 lessons
        Lessons l1 = new Lessons();
        l1.setLessonId(1L);
        Lessons l2 = new Lessons();
        l2.setLessonId(2L);
        List<Lessons> lessons = List.of(l1, l2);
        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(course.getCourseId()))
                .thenReturn(lessons);

        // 2 completed lesson progresses
        when(studentRepository.findByUser_Id(studentUser.getId())).thenReturn(Optional.of(studentProfile));
        LessonProgress lp1 = LessonProgress.builder().lesson(l1).isCompleted(true).build();
        LessonProgress lp2 = LessonProgress.builder().lesson(l2).isCompleted(true).build();
        when(lessonProgressRepository.findByStudentAndLessonIn(eq(studentProfile), any()))
                .thenReturn(List.of(lp1, lp2));

        // 1 quiz
        Sections s = new Sections();
        s.setCourse(course);
        l1.setSection(s);
        Quizzes q = new Quizzes();
        q.setQuizId(50L);
        q.setLesson(l1);
        q.setTotalPoint(50.0);
        when(quizRepository.findAll()).thenReturn(List.of(q));

        QuizAttempt qa = new QuizAttempt();
        qa.setUser(studentUser);
        qa.setQuiz(q);
        qa.setEarnedPoints(50.0);
        when(quizAttemptRepository.findAll()).thenReturn(List.of(qa));

        // 1 assignment
        Assignment a = Assignment.builder().assignmentId(60L).course(course).maxScore(50.0).build();
        when(assignmentRepository.findByCourse_CourseIdOrderByCreatedAtDesc(course.getCourseId()))
                .thenReturn(List.of(a));

        AssignmentSubmission sub = AssignmentSubmission.builder()
                .assignment(a)
                .student(studentUser)
                .status(AssignmentStatus.GRADED)
                .score(50.0)
                .build();
        when(submissionRepository.findByStudent_Id(studentUser.getId())).thenReturn(List.of(sub));

        // Act
        CourseProgressDTO progress = progressService.getCourseProgress(course.getCourseId(), studentUser.getEmail());

        // Assert
        assertEquals(100.0, progress.progressPercentage());
        assertTrue(progress.isCompleted());
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());

        // Verify notification and activity log were created
        verify(activityLogRepository).save(any(ActivityLog.class));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Continue learning returns next incomplete lesson")
    void testContinueLearning_ReturnsNextIncompleteLesson() {
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(course.getCourseId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUser_Email(studentUser.getEmail())).thenReturn(List.of(enrollment));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(studentUser.getId(), course.getCourseId()))
                .thenReturn(Optional.of(enrollment));

        Lessons l1 = new Lessons();
        l1.setLessonId(101L);
        l1.setTitle("Introduction to DWV Systems");
        l1.setOrderIndex(1);

        Lessons l2 = new Lessons();
        l2.setLessonId(102L);
        l2.setTitle("Water Distribution Basics");
        l2.setOrderIndex(2);

        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(course.getCourseId()))
                .thenReturn(List.of(l1, l2));

        when(studentRepository.findByUser_Id(studentUser.getId())).thenReturn(Optional.of(studentProfile));
        LessonProgress lp1 = LessonProgress.builder()
                .lesson(l1)
                .isCompleted(true)
                .lastPlaybackPositionSeconds(120.0)
                .build();
        when(lessonProgressRepository.findByStudentAndLessonIn(eq(studentProfile), any()))
                .thenReturn(List.of(lp1));

        ContinueLearningDTO continueDTO = progressService.getContinueLearning(studentUser.getEmail());

        assertNotNull(continueDTO);
        assertEquals(102L, continueDTO.nextLessonId());
        assertEquals("Water Distribution Basics", continueDTO.nextLessonTitle());
        assertEquals(2, continueDTO.nextLessonOrderIndex());
    }

    // ==========================================
    // 3. Certificate Claim, Verify & Template Tests
    // ==========================================

    @Test
    @DisplayName("Claim certificate fails if course is not 100% completed")
    void testClaimCertificate_FailsWhenIncomplete() {
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(course.getCourseId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(studentUser.getId(), course.getCourseId()))
                .thenReturn(Optional.of(enrollment));
        when(certificateRepository.findByStudent_IdAndCourse_CourseId(studentUser.getId(), course.getCourseId()))
                .thenReturn(Optional.empty());

        // 2 lessons, only 1 completed (50%)
        Lessons l1 = new Lessons();
        l1.setLessonId(1L);
        Lessons l2 = new Lessons();
        l2.setLessonId(2L);
        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(course.getCourseId()))
                .thenReturn(List.of(l1, l2));
        when(studentRepository.findByUser_Id(studentUser.getId())).thenReturn(Optional.of(studentProfile));
        LessonProgress lp1 = LessonProgress.builder().lesson(l1).isCompleted(true).build();
        when(lessonProgressRepository.findByStudentAndLessonIn(eq(studentProfile), any()))
                .thenReturn(List.of(lp1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                certificateService.claimCertificate(course.getCourseId(), studentUser.getEmail())
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Course is not 100% completed yet"));
    }

    @Test
    @DisplayName("Claim certificate succeeds when 100% complete and saves unique certificate code")
    void testClaimCertificate_SucceedsWhen100Percent() {
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(course.getCourseId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(studentUser.getId(), course.getCourseId()))
                .thenReturn(Optional.of(enrollment));
        when(certificateRepository.findByStudent_IdAndCourse_CourseId(studentUser.getId(), course.getCourseId()))
                .thenReturn(Optional.empty());

        // 1 lesson completed -> 100%
        Lessons l1 = new Lessons();
        l1.setLessonId(1L);
        when(lessonRepository.findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(course.getCourseId()))
                .thenReturn(List.of(l1));
        when(studentRepository.findByUser_Id(studentUser.getId())).thenReturn(Optional.of(studentProfile));
        LessonProgress lp1 = LessonProgress.builder().lesson(l1).isCompleted(true).build();
        when(lessonProgressRepository.findByStudentAndLessonIn(eq(studentProfile), any()))
                .thenReturn(List.of(lp1));

        when(certificateRepository.existsByCertificateCode(any())).thenReturn(false);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate c = invocation.getArgument(0);
            c.setCertificateId(1L);
            c.setIssuedAt(new Timestamp(System.currentTimeMillis()));
            return c;
        });

        CertificateResponseDTO result = certificateService.claimCertificate(course.getCourseId(), studentUser.getEmail());

        assertNotNull(result);
        assertNotNull(result.certificateCode());
        assertTrue(result.certificateCode().startsWith("DA-2026-03-"));
        assertEquals("Anna S. Martinez", result.studentName());
        assertEquals("Plumbing Journeyman Readiness Program", result.courseTitle());

        verify(certificateRepository).save(any(Certificate.class));
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
        verify(activityLogRepository, atLeastOnce()).save(any(ActivityLog.class));
    }

    @Test
    @DisplayName("Public certificate verification endpoint returns valid certificate info")
    void testVerifyCertificate_ValidCode() {
        Certificate cert = Certificate.builder()
                .certificateId(1L)
                .certificateCode("DA-2024-03-4782")
                .student(studentUser)
                .course(course)
                .finalScore(95.0)
                .issuedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(certificateRepository.findByCertificateCode("DA-2024-03-4782")).thenReturn(Optional.of(cert));

        CertificateVerifyDTO verifyDTO = certificateService.verifyCertificate("DA-2024-03-4782");

        assertTrue(verifyDTO.isValid());
        assertEquals("DA-2024-03-4782", verifyDTO.certificateCode());
        assertEquals("Anna S. Martinez", verifyDTO.studentName());
        assertEquals("Plumbing Journeyman Readiness Program", verifyDTO.courseTitle());
        assertEquals("16 Weeks (240 Hours)", verifyDTO.programDuration());
        assertEquals("Lumen LMS Learning Platform", verifyDTO.issuedBy());
    }

    @Test
    @DisplayName("Render certificate HTML returns template matching design with student name and Lumen LMS branding")
    void testRenderCertificateHtml_ContainsTemplateElements() {
        Certificate cert = Certificate.builder()
                .certificateId(1L)
                .certificateCode("DA-2024-03-4782")
                .student(studentUser)
                .course(course)
                .finalScore(98.5)
                .issuedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(certificateRepository.findByCertificateCode("DA-2024-03-4782")).thenReturn(Optional.of(cert));

        String html = certificateService.renderCertificateHtml("DA-2024-03-4782");

        assertNotNull(html);
        assertTrue(html.contains("COURSE COMPLETION"), "Should have COURSE COMPLETION header");
        assertTrue(html.contains("Certificate"), "Should have Certificate subtitle");
        assertTrue(html.contains("Anna S. Martinez"), "Should have recipient student name");
        assertTrue(html.contains("Plumbing Journeyman Readiness Program"), "Should have course title");
        assertTrue(html.contains("DA-2024-03-4782"), "Should have certificate code");
        assertTrue(html.contains("16 Weeks (240 Hours)"), "Should have duration");
        assertTrue(html.contains("Lumen"), "Should have Lumen LMS branding");
        assertTrue(html.contains("Jennifer Walsh"), "Should have Head of Curriculum signature");
        assertFalse(html.contains("class=\"sidebar\""), "Should not have right sidebar");
        assertFalse(html.contains("www.techlearn.com/verify"), "Should not have right-side verification URL");
    }

    // ==========================================
    // 4. Notifications & Activity Log Endpoints
    // ==========================================

    @Test
    @DisplayName("Notifications unread count and mark-as-read endpoints work as expected")
    void testNotificationsFlow() throws Exception {
        Principal principal = new UsernamePasswordAuthenticationToken("anna@example.com", null);

        Notification n = Notification.builder()
                .id(101L)
                .user(studentUser)
                .title("Welcome to LMS")
                .message("Start learning!")
                .type("SYSTEM")
                .isRead(false)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(notificationRepository.findByUser_EmailOrderByCreatedAtDesc("anna@example.com"))
                .thenReturn(List.of(n));
        when(notificationRepository.countByUser_EmailAndIsReadFalse("anna@example.com"))
                .thenReturn(1L);

        // GET /api/notifications
        mockMvcNotification.perform(get("/api/notifications").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Welcome to LMS"));

        // GET /api/notifications/unread-count
        mockMvcNotification.perform(get("/api/notifications/unread-count").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));

        // PATCH /api/notifications/101/read
        when(notificationRepository.findById(101L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        mockMvcNotification.perform(patch("/api/notifications/101/read").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    @DisplayName("Activity log endpoint returns student activity timeline")
    void testActivityLogFlow() throws Exception {
        Principal principal = new UsernamePasswordAuthenticationToken("anna@example.com", null);

        ActivityLog log = ActivityLog.builder()
                .id(1L)
                .user(studentUser)
                .activityType("ENROLLED")
                .description("Enrolled in Plumbing Journeyman Readiness Program")
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();

        when(activityLogRepository.findByUser_EmailOrderByTimestampDesc("anna@example.com"))
                .thenReturn(List.of(log));

        mockMvcActivityLog.perform(get("/api/users/activities").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityType").value("ENROLLED"))
                .andExpect(jsonPath("$[0].description").value("Enrolled in Plumbing Journeyman Readiness Program"));
    }

    @Test
    @DisplayName("Public HTML certificate view endpoint serves text/html")
    void testPublicCertificateViewEndpoint() throws Exception {
        Certificate cert = Certificate.builder()
                .certificateId(1L)
                .certificateCode("DA-2024-03-4782")
                .student(studentUser)
                .course(course)
                .issuedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(certificateRepository.findByCertificateCode("DA-2024-03-4782")).thenReturn(Optional.of(cert));

        mockMvcCertificate.perform(get("/api/certificates/DA-2024-03-4782/view"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("COURSE COMPLETION")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Certificate")));
    }

    @Test
    @DisplayName("Spring Bean instantiation selects @Autowired constructor for multi-constructor services")
    void testSpringBeanFactoryInstantiatesServicesWithMultipleConstructors() {
        org.springframework.context.support.GenericApplicationContext ctx = new org.springframework.context.support.GenericApplicationContext();
        ctx.registerBean(org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.class);

        ctx.registerBean(AssignmentRepository.class, () -> assignmentRepository);
        ctx.registerBean(AssignmentSubmissionRepository.class, () -> submissionRepository);
        ctx.registerBean(CourseRepository.class, () -> courseRepository);
        ctx.registerBean(SectionRepository.class, () -> mock(SectionRepository.class));
        ctx.registerBean(UserRepository.class, () -> userRepository);
        ctx.registerBean(StudentRepository.class, () -> studentRepository);
        ctx.registerBean(EnrollmentRepository.class, () -> enrollmentRepository);
        ctx.registerBean(FileUploadService.class, () -> mock(FileUploadService.class));
        ctx.registerBean(StudentPointsService.class, () -> studentPointsService);
        ctx.registerBean(NotificationService.class, () -> notificationService);
        ctx.registerBean(ActivityLogService.class, () -> activityLogService);
        ctx.registerBean(ProgressService.class, () -> progressService);
        ctx.registerBean(QuizRepository.class, () -> quizRepository);
        ctx.registerBean(com.ly.lmsbackend.mapper.QuizMapper.class, () -> mock(com.ly.lmsbackend.mapper.QuizMapper.class));
        ctx.registerBean(LessonRepository.class, () -> lessonRepository);
        ctx.registerBean(QuizAttemptRepository.class, () -> quizAttemptRepository);
        ctx.registerBean(com.ly.lmsbackend.mapper.QuizAttemptMapper.class, () -> mock(com.ly.lmsbackend.mapper.QuizAttemptMapper.class));
        ctx.registerBean(LessonProgressRepository.class, () -> lessonProgressRepository);
        ctx.registerBean(com.ly.lmsbackend.mapper.EnrollmentMapper.class, () -> mock(com.ly.lmsbackend.mapper.EnrollmentMapper.class));

        ctx.registerBean(AssignmentService.class);
        ctx.registerBean(QuizService.class);
        ctx.registerBean(LessonProgressService.class);
        ctx.registerBean(EnrollmentService.class);

        ctx.refresh();

        assertNotNull(ctx.getBean(AssignmentService.class));
        assertNotNull(ctx.getBean(QuizService.class));
        assertNotNull(ctx.getBean(LessonProgressService.class));
        assertNotNull(ctx.getBean(EnrollmentService.class));

        ctx.close();
    }
}

