package com.ly.lmsbackend;

import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentCheckoutRequestDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentResponseDTO;
import com.ly.lmsbackend.mapper.PaymentMapper;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.PaymentRepository;
import com.ly.lmsbackend.repository.UserRepository;
import com.ly.lmsbackend.service.EnrollmentService;
import com.ly.lmsbackend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAbaPayWayTests {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentService enrollmentService;

    private PaymentMapper paymentMapper;
    private PaymentService paymentService;

    private Users studentUser;
    private Users adminUser;
    private Courses course;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
        paymentService = new PaymentService(
                paymentRepository,
                courseRepository,
                userRepository,
                enrollmentService,
                paymentMapper
        );

        studentUser = Users.builder()
                .id(1L)
                .username("student1")
                .email("student@test.com")
                .role(Roles.STUDENT)
                .build();

        adminUser = Users.builder()
                .id(99L)
                .username("admin1")
                .email("admin@test.com")
                .role(Roles.ADMIN)
                .build();

        course = new Courses();
        course.setCourseId(101L);
        course.setTitle("Full Stack Mastery");
        course.setPrice(BigDecimal.valueOf(25.50));
    }

    @Test
    @DisplayName("buildAbaPayWayUrl formats amount dynamically into URL")
    void testBuildAbaPayWayUrl() {
        String url = PaymentMapper.buildAbaPayWayUrl(BigDecimal.valueOf(25.50));
        assertTrue(url.contains("amount=25.50"), "URL should contain amount=25.50");
        assertTrue(url.startsWith("https://link.payway.com.kh/aba?id=E41BD42D5BCD"));
        assertTrue(url.contains("acc=004120485"));
    }

    @Test
    @DisplayName("createCheckout creates PENDING payment with ABA_PAYWAY provider and dynamic paymentUrl")
    void testCreateCheckoutAbaPayWay() {
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(101L)).thenReturn(Optional.of(course));
        when(paymentRepository.save(any(Payments.class))).thenAnswer(inv -> {
            Payments p = inv.getArgument(0);
            p.setPaymentId(501L);
            return p;
        });

        PaymentCheckoutRequestDTO checkoutDTO = new PaymentCheckoutRequestDTO(101L, null);
        PaymentResponseDTO response = paymentService.createCheckout(checkoutDTO, "student@test.com");

        assertNotNull(response);
        assertEquals(501L, response.paymentId());
        assertEquals("ABA_PAYWAY", response.provider());
        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(BigDecimal.valueOf(25.50), response.amount());
        assertNotNull(response.paymentUrl());
        assertTrue(response.paymentUrl().contains("amount=25.50"));
    }

    @Test
    @DisplayName("confirmPayment forbids student self-confirmation for paid courses")
    void testConfirmPaymentForbiddenForStudentOnPaidCourse() {
        Payments payment = new Payments();
        payment.setPaymentId(502L);
        payment.setUser(studentUser);
        payment.setCourse(course);
        payment.setAmount(BigDecimal.valueOf(25.50));
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(502L)).thenReturn(Optional.of(payment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                paymentService.confirmPayment(502L, "student@test.com")
        );

        assertTrue(ex.getReason().contains("admin confirmation"));
    }

    @Test
    @DisplayName("confirmPayment allows confirmation if course is free ($0.00)")
    void testConfirmPaymentAllowedForFreeCourse() {
        Courses freeCourse = new Courses();
        freeCourse.setCourseId(102L);
        freeCourse.setTitle("Free Overview");
        freeCourse.setPrice(BigDecimal.ZERO);

        Payments payment = new Payments();
        payment.setPaymentId(503L);
        payment.setUser(studentUser);
        payment.setCourse(freeCourse);
        payment.setAmount(BigDecimal.ZERO);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(503L)).thenReturn(Optional.of(payment));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(paymentRepository.save(any(Payments.class))).thenReturn(payment);

        PaymentResponseDTO response = paymentService.confirmPayment(503L, "student@test.com");

        assertEquals(PaymentStatus.PAID, response.status());
        verify(enrollmentService, times(1)).enrollCurrentUser(any(EnrollmentCreateDTO.class), eq("student@test.com"));
    }

    @Test
    @DisplayName("updateStatus to PAID by admin confirms payment and enrolls student")
    void testAdminUpdateStatusEnrollsStudent() {
        Payments payment = new Payments();
        payment.setPaymentId(504L);
        payment.setUser(studentUser);
        payment.setCourse(course);
        payment.setAmount(BigDecimal.valueOf(25.50));
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(504L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payments.class))).thenReturn(payment);

        PaymentResponseDTO response = paymentService.updateStatus(504L, PaymentStatus.PAID, "admin-confirmed");

        assertEquals(PaymentStatus.PAID, response.status());
        assertEquals("admin-confirmed", response.providerReference());
        verify(enrollmentService, times(1)).enrollCurrentUser(any(EnrollmentCreateDTO.class), eq("student@test.com"));
    }
}
