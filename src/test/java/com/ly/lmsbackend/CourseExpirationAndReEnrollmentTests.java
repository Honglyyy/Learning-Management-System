package com.ly.lmsbackend;

import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentResponseDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonQuizDTO;
import com.ly.lmsbackend.dto.paymentdtos.CheckoutQuoteDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentCheckoutRequestDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentResponseDTO;
import com.ly.lmsbackend.mapper.EnrollmentMapper;
import com.ly.lmsbackend.mapper.LessonMapper;
import com.ly.lmsbackend.mapper.PaymentMapper;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import com.ly.lmsbackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CourseExpirationAndReEnrollmentTests {

    private EnrollmentRepository enrollmentRepository;
    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private PaymentRepository paymentRepository;
    private EnrollmentMapper enrollmentMapper;
    private ActivityLogService activityLogService;
    private NotificationService notificationService;
    private QuizAttemptRepository quizAttemptRepository;

    private EnrollmentService enrollmentService;

    private Users student;
    private Courses defaultCourse;
    private Courses customDurationCourse;

    @BeforeEach
    void setUp() {
        enrollmentRepository = Mockito.mock(EnrollmentRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        courseRepository = Mockito.mock(CourseRepository.class);
        paymentRepository = Mockito.mock(PaymentRepository.class);
        enrollmentMapper = new EnrollmentMapper();
        activityLogService = Mockito.mock(ActivityLogService.class);
        notificationService = Mockito.mock(NotificationService.class);
        quizAttemptRepository = Mockito.mock(QuizAttemptRepository.class);

        enrollmentService = new EnrollmentService(
                enrollmentRepository,
                userRepository,
                courseRepository,
                enrollmentMapper,
                quizAttemptRepository,
                activityLogService,
                notificationService,
                paymentRepository
        );

        student = Users.builder()
                .id(100L)
                .username("student1")
                .email("student1@example.com")
                .role(Roles.STUDENT)
                .build();

        defaultCourse = new Courses();
        defaultCourse.setCourseId(1L);
        defaultCourse.setTitle("Standard Course");
        defaultCourse.setPrice(BigDecimal.valueOf(100.00));
        defaultCourse.setAccessDurationDays(180);
        defaultCourse.setStatus(CourseStatus.PUBLISHED);

        customDurationCourse = new Courses();
        customDurationCourse.setCourseId(2L);
        customDurationCourse.setTitle("Intensive 30-Day Bootcamp");
        customDurationCourse.setPrice(BigDecimal.valueOf(200.00));
        customDurationCourse.setAccessDurationDays(30);
        customDurationCourse.setStatus(CourseStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Enrollment calculates expiration date based on course access duration (default 180 days)")
    void testEnrollmentSetsExpirationDateDefaultCourse() {
        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(defaultCourse));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), 1L))
                .thenReturn(Optional.empty());

        // Free course enrollment for zero price testing
        defaultCourse.setPrice(BigDecimal.ZERO);

        ArgumentCaptor<Enrollments> captor = ArgumentCaptor.forClass(Enrollments.class);
        when(enrollmentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentCreateDTO dto = new EnrollmentCreateDTO(1L);
        EnrollmentResponseDTO response = enrollmentService.enrollCurrentUser(dto, "student1@example.com");

        Enrollments saved = captor.getValue();
        assertNotNull(saved.getExpirationDate(), "Expiration date must be set upon enrollment");

        Timestamp minTime = Timestamp.from(Instant.now().plus(179, ChronoUnit.DAYS));
        Timestamp maxTime = Timestamp.from(Instant.now().plus(181, ChronoUnit.DAYS));
        assertTrue(saved.getExpirationDate().after(minTime) && saved.getExpirationDate().before(maxTime),
                "Expiration date must be ~180 days from enrollment");
        assertFalse(saved.isExpired(), "New enrollment should not be expired");
        assertTrue(saved.isActive(), "New enrollment should be active");
    }

    @Test
    @DisplayName("Enrollment calculates expiration date based on custom course access duration (e.g. 30 days)")
    void testEnrollmentSetsExpirationDateCustomDuration() {
        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(customDurationCourse));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), 2L))
                .thenReturn(Optional.empty());

        customDurationCourse.setPrice(BigDecimal.ZERO);

        ArgumentCaptor<Enrollments> captor = ArgumentCaptor.forClass(Enrollments.class);
        when(enrollmentRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentCreateDTO dto = new EnrollmentCreateDTO(2L);
        enrollmentService.enrollCurrentUser(dto, "student1@example.com");

        Enrollments saved = captor.getValue();
        assertNotNull(saved.getExpirationDate());

        Timestamp minTime = Timestamp.from(Instant.now().plus(29, ChronoUnit.DAYS));
        Timestamp maxTime = Timestamp.from(Instant.now().plus(31, ChronoUnit.DAYS));
        assertTrue(saved.getExpirationDate().after(minTime) && saved.getExpirationDate().before(maxTime),
                "Expiration date must be ~30 days from enrollment");
    }

    @Test
    @DisplayName("LessonService throws 403 Forbidden when student access is expired")
    void testLessonServiceThrows403WhenExpired() {
        LessonRepository lessonRepository = Mockito.mock(LessonRepository.class);
        SectionRepository sectionRepository = Mockito.mock(SectionRepository.class);
        QuizRepository quizRepository = Mockito.mock(QuizRepository.class);
        LessonProgressRepository lessonProgressRepository = Mockito.mock(LessonProgressRepository.class);
        LessonMapper lessonMapper = Mockito.mock(LessonMapper.class);

        LessonService lessonService = new LessonService(
                lessonMapper,
                lessonRepository,
                sectionRepository,
                courseRepository,
                quizRepository,
                quizAttemptRepository,
                lessonProgressRepository,
                userRepository,
                enrollmentRepository
        );

        Sections section = new Sections();
        section.setCourse(defaultCourse);

        Lessons paidLesson = new Lessons();
        paidLesson.setLessonId(10L);
        paidLesson.setTitle("Advanced Architecture");
        paidLesson.setSection(section);
        paidLesson.setIsFree(false);

        when(lessonRepository.findById(10L)).thenReturn(Optional.of(paidLesson));
        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));

        // Create expired enrollment
        Enrollments expiredEnrollment = new Enrollments();
        expiredEnrollment.setUser(student);
        expiredEnrollment.setCourse(defaultCourse);
        expiredEnrollment.setStatus(EnrollmentStatus.ACTIVE);
        expiredEnrollment.setExpirationDate(Timestamp.from(Instant.now().minus(5, ChronoUnit.DAYS))); // expired 5 days ago

        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), defaultCourse.getCourseId()))
                .thenReturn(Optional.of(expiredEnrollment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                lessonService.getLesson(10L, "student1@example.com")
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("expired"), "Error message should mention expired access");
    }

    @Test
    @DisplayName("LessonService allows free trial lessons even when access is expired")
    void testLessonServiceAllowsFreeLessonWhenExpired() {
        LessonRepository lessonRepository = Mockito.mock(LessonRepository.class);
        SectionRepository sectionRepository = Mockito.mock(SectionRepository.class);
        QuizRepository quizRepository = Mockito.mock(QuizRepository.class);
        LessonProgressRepository lessonProgressRepository = Mockito.mock(LessonProgressRepository.class);
        LessonMapper lessonMapper = Mockito.mock(LessonMapper.class);

        LessonService lessonService = new LessonService(
                lessonMapper,
                lessonRepository,
                sectionRepository,
                courseRepository,
                quizRepository,
                quizAttemptRepository,
                lessonProgressRepository,
                userRepository,
                enrollmentRepository
        );

        Sections section = new Sections();
        section.setCourse(defaultCourse);

        Lessons freeLesson = new Lessons();
        freeLesson.setLessonId(11L);
        freeLesson.setTitle("Introduction Preview");
        freeLesson.setSection(section);
        freeLesson.setIsFree(true);

        when(lessonRepository.findById(11L)).thenReturn(Optional.of(freeLesson));
        when(quizRepository.findByLesson_LessonId(11L)).thenReturn(Collections.emptyList());

        LessonQuizDTO result = lessonService.getLesson(11L, "student1@example.com");
        assertNotNull(result);
        assertEquals("Introduction Preview", result.title());
    }

    @Test
    @DisplayName("LessonMaterialService throws 403 Forbidden when student access has expired")
    void testLessonMaterialServiceThrows403WhenExpired() {
        LessonMaterialRepository materialRepository = Mockito.mock(LessonMaterialRepository.class);
        LessonRepository lessonRepository = Mockito.mock(LessonRepository.class);
        StudentRepository studentRepository = Mockito.mock(StudentRepository.class);
        FileUploadService fileUploadService = Mockito.mock(FileUploadService.class);

        LessonMaterialService materialService = new LessonMaterialService(
                materialRepository,
                lessonRepository,
                studentRepository,
                enrollmentRepository,
                userRepository,
                fileUploadService
        );

        Sections section = new Sections();
        section.setCourse(defaultCourse);

        Lessons lesson = new Lessons();
        lesson.setLessonId(15L);
        lesson.setTitle("Materials Lesson");
        lesson.setSection(section);
        lesson.setIsFree(false);

        when(lessonRepository.findById(15L)).thenReturn(Optional.of(lesson));
        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));

        Students studentProfile = new Students();
        studentProfile.setUser(student);
        when(studentRepository.findByUser_Email("student1@example.com")).thenReturn(Optional.of(studentProfile));

        Enrollments expiredEnrollment = new Enrollments();
        expiredEnrollment.setUser(student);
        expiredEnrollment.setCourse(defaultCourse);
        expiredEnrollment.setStatus(EnrollmentStatus.ACTIVE);
        expiredEnrollment.setExpirationDate(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));

        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), defaultCourse.getCourseId()))
                .thenReturn(Optional.of(expiredEnrollment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                materialService.getMaterials(15L, "student1@example.com")
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("expired"));
    }

    @Test
    @DisplayName("PaymentService applies 50% re-enrollment discount when previous enrollment has expired")
    void testPaymentServiceApplies50PercentDiscountForExpiredStudent() {
        PaymentMapper paymentMapper = new PaymentMapper();
        EmailService emailService = Mockito.mock(EmailService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                courseRepository,
                userRepository,
                enrollmentService,
                paymentMapper,
                emailService,
                notificationService,
                enrollmentRepository
        );

        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(defaultCourse));

        // Expired enrollment exists for this user and course
        Enrollments expiredEnrollment = new Enrollments();
        expiredEnrollment.setUser(student);
        expiredEnrollment.setCourse(defaultCourse);
        expiredEnrollment.setExpirationDate(Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)));
        expiredEnrollment.setStatus(EnrollmentStatus.EXPIRED);

        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), defaultCourse.getCourseId()))
                .thenReturn(Optional.of(expiredEnrollment));

        ArgumentCaptor<Payments> paymentCaptor = ArgumentCaptor.forClass(Payments.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(inv -> {
            Payments p = inv.getArgument(0);
            p.setPaymentId(999L);
            return p;
        });

        PaymentCheckoutRequestDTO checkoutDTO = new PaymentCheckoutRequestDTO(1L, "ABA_PAYWAY");
        PaymentResponseDTO response = paymentService.createCheckout(checkoutDTO, "student1@example.com");

        Payments savedPayment = paymentCaptor.getValue();
        assertTrue(savedPayment.getIsReEnrollmentDiscount(), "Payment must be marked as re-enrollment discount");
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(savedPayment.getOriginalAmount()));
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(savedPayment.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(savedPayment.getAmount()), "Final price must be 50% of 100.00");

        assertTrue(response.isReEnrollmentDiscount());
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(response.amount()));
    }

    @Test
    @DisplayName("calculateCheckoutQuote accurately computes 50% discount breakdown for expired enrollment")
    void testCalculateCheckoutQuoteReturnsDiscount() {
        PaymentMapper paymentMapper = new PaymentMapper();
        EmailService emailService = Mockito.mock(EmailService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                courseRepository,
                userRepository,
                enrollmentService,
                paymentMapper,
                emailService,
                notificationService,
                enrollmentRepository
        );

        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(defaultCourse));

        Enrollments expiredEnrollment = new Enrollments();
        expiredEnrollment.setUser(student);
        expiredEnrollment.setCourse(defaultCourse);
        expiredEnrollment.setExpirationDate(Timestamp.from(Instant.now().minus(10, ChronoUnit.DAYS)));
        expiredEnrollment.setStatus(EnrollmentStatus.EXPIRED);

        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), defaultCourse.getCourseId()))
                .thenReturn(Optional.of(expiredEnrollment));

        CheckoutQuoteDTO quote = paymentService.calculateCheckoutQuote(1L, "student1@example.com");

        assertTrue(quote.isReEnrollmentDiscount());
        assertTrue(quote.isExpired());
        assertEquals(50.0, quote.discountPercentage());
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(quote.originalPrice()));
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(quote.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(quote.finalPrice()));
    }

    @Test
    @DisplayName("calculateCheckoutQuote charges full price when user has no expired enrollment")
    void testCalculateCheckoutQuoteFullPriceForNewStudent() {
        PaymentMapper paymentMapper = new PaymentMapper();
        EmailService emailService = Mockito.mock(EmailService.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                courseRepository,
                userRepository,
                enrollmentService,
                paymentMapper,
                emailService,
                notificationService,
                enrollmentRepository
        );

        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(defaultCourse));

        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), defaultCourse.getCourseId()))
                .thenReturn(Optional.empty());

        CheckoutQuoteDTO quote = paymentService.calculateCheckoutQuote(1L, "student1@example.com");

        assertFalse(quote.isReEnrollmentDiscount());
        assertFalse(quote.isExpired());
        assertEquals(0.0, quote.discountPercentage());
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(quote.originalPrice()));
        assertEquals(0, BigDecimal.ZERO.compareTo(quote.discountAmount()));
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(quote.finalPrice()));
    }

    @Test
    @DisplayName("Student with expired enrollment can re-enroll after payment and gets renewed expiration date")
    void testReEnrollmentReactivatesAccessWithNewExpiration() {
        when(userRepository.findByEmail("student1@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(defaultCourse));

        Enrollments expiredEnrollment = new Enrollments();
        expiredEnrollment.setEnrollmentId(50L);
        expiredEnrollment.setUser(student);
        expiredEnrollment.setCourse(defaultCourse);
        expiredEnrollment.setExpirationDate(Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)));
        expiredEnrollment.setStatus(EnrollmentStatus.ACTIVE); // status was ACTIVE, but date in past

        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(student.getId(), 1L))
                .thenReturn(Optional.of(expiredEnrollment));

        // Successful payment after expiration
        Payments recentPayment = new Payments();
        recentPayment.setPaymentId(101L);
        recentPayment.setUser(student);
        recentPayment.setCourse(defaultCourse);
        recentPayment.setStatus(PaymentStatus.PAID);
        recentPayment.setIsReEnrollmentDiscount(true);
        recentPayment.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        when(paymentRepository.findFirstByUser_EmailAndCourse_CourseIdAndStatusOrderByCreatedAtDesc(
                "student1@example.com", 1L, PaymentStatus.PAID))
                .thenReturn(Optional.of(recentPayment));

        when(enrollmentRepository.save(any(Enrollments.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentCreateDTO dto = new EnrollmentCreateDTO(1L);
        EnrollmentResponseDTO response = enrollmentService.enrollCurrentUser(dto, "student1@example.com");

        assertEquals(EnrollmentStatus.ACTIVE, expiredEnrollment.getStatus());
        assertFalse(expiredEnrollment.isExpired(), "Re-enrolled record must not be expired");

        Timestamp minExpected = Timestamp.from(Instant.now().plus(170, ChronoUnit.DAYS));
        assertTrue(expiredEnrollment.getExpirationDate().after(minExpected),
                "New expiration date must be ~180 days in the future");
    }
}
