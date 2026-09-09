package com.ly.lmsbackend;

import com.ly.lmsbackend.controller.UserController;
import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizDetailDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizSubmitDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.CertificateService;
import com.ly.lmsbackend.service.EnrollmentService;
import com.ly.lmsbackend.service.QuizService;
import com.ly.lmsbackend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductionReadinessSecurityTests {

    @Test
    @DisplayName("CertificateService escapes HTML in invalid certificate code (Reflected XSS)")
    void testCertificateXssReflectedEscaping() {
        CertificateRepository certificateRepository = Mockito.mock(CertificateRepository.class);
        when(certificateRepository.findByCertificateCode(any())).thenReturn(Optional.empty());

        CertificateService certificateService = new CertificateService(
                certificateRepository, null, null, null, null, null, null
        );
        String maliciousCode = "<script>alert('xss')</script>";
        String html = certificateService.renderCertificateHtml(maliciousCode);

        assertFalse(html.contains("<script>alert('xss')</script>"));
        assertTrue(html.contains("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"));
    }

    @Test
    @DisplayName("EnrollmentService blocks free enrollment on paid courses without confirmed payment")
    void testPaidEnrollmentProtection() {
        EnrollmentRepository enrollmentRepository = Mockito.mock(EnrollmentRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        CourseRepository courseRepository = Mockito.mock(CourseRepository.class);
        PaymentRepository paymentRepository = Mockito.mock(PaymentRepository.class);

        Courses paidCourse = new Courses();
        paidCourse.setCourseId(10L);
        paidCourse.setPrice(BigDecimal.valueOf(99.0));

        Users student = new Users();
        student.setEmail("student@example.com");

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(paidCourse));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(any(), eq(10L))).thenReturn(Optional.empty());
        when(paymentRepository.findFirstByUser_EmailAndCourse_CourseIdAndStatusOrderByCreatedAtDesc(
                "student@example.com", 10L, PaymentStatus.PAID)).thenReturn(Optional.empty());

        EnrollmentService enrollmentService = new EnrollmentService(
                enrollmentRepository, userRepository, courseRepository, null, null, null, null, paymentRepository
        );

        EnrollmentCreateDTO dto = new EnrollmentCreateDTO(10L);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                enrollmentService.enrollCurrentUser(dto, "student@example.com")
        );

        assertEquals(HttpStatus.PAYMENT_REQUIRED, exception.getStatusCode());
    }

    @Test
    @DisplayName("QuizService masks isCorrect for students who have not yet submitted an attempt")
    void testQuizAnswerMaskingBeforeSubmission() {
        QuizRepository quizRepository = Mockito.mock(QuizRepository.class);
        QuizAttemptRepository quizAttemptRepository = Mockito.mock(QuizAttemptRepository.class);

        Quizzes quiz = new Quizzes();
        quiz.setQuizId(1L);
        quiz.setQuizTitle("Security Basics");
        quiz.setTotalPoint(100.0);

        Questions question = new Questions();
        question.setQuestionId(1L);
        question.setQuestionText("What is XSS?");
        question.setPoint(10L);

        Answers a1 = new Answers();
        a1.setAnswerId(101L);
        a1.setAnswerText("Cross Site Scripting");
        a1.setIsCorrect(true);

        Answers a2 = new Answers();
        a2.setAnswerId(102L);
        a2.setAnswerText("Extra Style Sheets");
        a2.setIsCorrect(false);

        question.setAnswers(List.of(a1, a2));
        quiz.setQuestions(List.of(question));

        when(quizRepository.findByLesson_LessonId(50L)).thenReturn(List.of(quiz));
        when(quizAttemptRepository.findByUser_EmailAndQuiz_QuizId("student@example.com", 1L)).thenReturn(Optional.empty());

        QuizService quizService = new QuizService(
                quizRepository, null, null, null, quizAttemptRepository, null, null, null, null
        );

        QuizDetailDTO dto = quizService.getQuizByLesson(50L, "student@example.com", false);

        assertNotNull(dto);
        assertEquals(1, dto.question().size());
        assertEquals(2, dto.question().get(0).answers().size());
        assertNull(dto.question().get(0).answers().get(0).isCorrect());
        assertNull(dto.question().get(0).answers().get(1).isCorrect());
    }

    @Test
    @DisplayName("QuizService prevents cheating when user submits all answer options")
    void testQuizCheatingPrevention() {
        QuizRepository quizRepository = Mockito.mock(QuizRepository.class);
        EnrollmentRepository enrollmentRepository = Mockito.mock(EnrollmentRepository.class);
        QuizAttemptRepository quizAttemptRepository = Mockito.mock(QuizAttemptRepository.class);

        Courses course = new Courses();
        course.setCourseId(1L);

        Sections section = new Sections();
        section.setCourse(course);

        Lessons lesson = new Lessons();
        lesson.setSection(section);

        Quizzes quiz = new Quizzes();
        quiz.setQuizId(1L);
        quiz.setLesson(lesson);
        quiz.setTotalPoint(10.0);

        Questions question = new Questions();
        question.setQuestionId(1L);
        question.setPoint(10L);

        Answers a1 = new Answers();
        a1.setAnswerId(101L);
        a1.setIsCorrect(true);

        Answers a2 = new Answers();
        a2.setAnswerId(102L);
        a2.setIsCorrect(false);

        question.setAnswers(List.of(a1, a2));
        quiz.setQuestions(List.of(question));

        Enrollments enrollment = new Enrollments();
        enrollment.setEnrollmentId(99L);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.findByUser_EmailAndCourse_CourseId("student@example.com", 1L)).thenReturn(Optional.of(enrollment));
        when(quizAttemptRepository.findByEnrollment_EnrollmentIdAndQuiz_QuizId(99L, 1L)).thenReturn(Optional.empty());
        when(quizAttemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizService quizService = new QuizService(
                quizRepository, null, null, enrollmentRepository, quizAttemptRepository, null, null, null, null
        );

        // Student submits both correct and incorrect answers to cheat
        QuizSubmitDTO cheatingDto = new QuizSubmitDTO(List.of(101L, 102L));
        try {
            quizService.submitQuiz(1L, cheatingDto, "student@example.com");
        } catch (Exception ignored) {}

        // Attempt should be saved with 0 points earned because 2 answers were selected for 1 single-choice question
        Mockito.verify(quizAttemptRepository).save(Mockito.argThat(attempt ->
                attempt.getEarnedPoints() == 0.0 && attempt.getCorrectAnswers() == 0
        ));
    }

    @Test
    @DisplayName("UserController /send-reset-otp accepts JSON request body")
    void testSendResetOtpWithJsonBody() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/send-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset code sent successfully"));

        Mockito.verify(userService).sendResetOtp(eq("user@example.com"));
    }
}
