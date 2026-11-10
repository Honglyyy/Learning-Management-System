package com.ly.lmsbackend;

import com.ly.lmsbackend.config.BakongProperties;
import com.ly.lmsbackend.controller.PaymentController;
import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentCheckoutRequestDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentResponseDTO;
import com.ly.lmsbackend.mapper.PaymentMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.EnrollmentStatus;
import com.ly.lmsbackend.model.Enrollments;
import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.EnrollmentRepository;
import com.ly.lmsbackend.repository.PaymentRepository;
import com.ly.lmsbackend.repository.UserRepository;
import com.ly.lmsbackend.service.BakongGatewayService;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.service.EnrollmentService;
import com.ly.lmsbackend.service.NotificationService;
import com.ly.lmsbackend.service.PaymentReconciliationScheduler;
import com.ly.lmsbackend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BakongPaymentIntegrationTests {

    private PaymentRepository paymentRepository;
    private CourseRepository courseRepository;
    private UserRepository userRepository;
    private EnrollmentRepository enrollmentRepository;
    private EnrollmentService enrollmentService;
    private EmailService emailService;
    private NotificationService notificationService;

    private Users student;
    private Users otherStudent;
    private Users instructor;
    private Courses paidCourse;

    @BeforeEach
    void setUp() {
        paymentRepository = Mockito.mock(PaymentRepository.class);
        courseRepository = Mockito.mock(CourseRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        enrollmentRepository = Mockito.mock(EnrollmentRepository.class);
        enrollmentService = Mockito.mock(EnrollmentService.class);
        emailService = Mockito.mock(EmailService.class);
        notificationService = Mockito.mock(NotificationService.class);

        student = new Users();
        student.setId(10L);
        student.setUsername("sokha");
        student.setEmail("sokha@example.com");
        student.setRole(Roles.STUDENT);

        otherStudent = new Users();
        otherStudent.setId(11L);
        otherStudent.setUsername("dara");
        otherStudent.setEmail("dara@example.com");
        otherStudent.setRole(Roles.STUDENT);

        instructor = new Users();
        instructor.setId(20L);
        instructor.setUsername("instructor");
        instructor.setFullname("Lumen Instructor");
        instructor.setEmail("instructor@example.com");
        instructor.setRole(Roles.INSTRUCTOR);

        paidCourse = new Courses();
        paidCourse.setCourseId(5L);
        paidCourse.setTitle("Flutter & Spring Boot Mastery");
        paidCourse.setPrice(BigDecimal.valueOf(80.00));
        paidCourse.setAccessDurationDays(180);
        paidCourse.setInstructor(instructor);
    }

    @Test
    @DisplayName("BakongGatewayService fails cleanly with 503 when token or account ID is empty")
    void testUnconfiguredSecretsThrow503() {
        BakongProperties props = new BakongProperties();
        // Kept empty intentionally
        props.getApi().setToken("");
        props.setAccountId("");

        BakongGatewayService service = new BakongGatewayService(props, RestClient.builder().build());

        assertFalse(service.isConfigured());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.createDynamicPayment(BigDecimal.valueOf(10.00), "BK-123", Instant.now().plus(15, ChronoUnit.MINUTES))
        );
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }

    @Test
    @DisplayName("BakongGatewayService generates dynamic KHQR payload with real NBC SDK and handles deeplink + MD5 verification")
    void testRealSdkGenerationAndMockedBakongHttpCalls() {
        BakongProperties props = new BakongProperties();
        props.getApi().setBaseUrl("https://sit-api-bakong.nbc.gov.kh");
        props.getApi().setToken("fake-sit-jwt-token");
        props.setAccountId("lumen_account@devb");
        props.setAccountName("Lumen LMS");
        props.setCity("Phnom Penh");
        props.setCurrency("USD");

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://sit-api-bakong.nbc.gov.kh/v1/generate_deeplink_by_qr"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer fake-sit-jwt-token"))
                .andRespond(withSuccess("{\"responseCode\":0,\"data\":{\"shortLink\":\"https://bakong.page.link/abc\"}}", MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://sit-api-bakong.nbc.gov.kh/v1/check_transaction_by_md5"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"responseCode\":1,\"errorCode\":1,\"responseMessage\":\"Transaction could not be found\"}", MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://sit-api-bakong.nbc.gov.kh/v1/check_transaction_by_md5"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"responseCode\":0,\"data\":{\"hash\":\"txhash123\",\"fromAccountId\":\"payer@aba\",\"toAccountId\":\"lumen_account@devb\",\"currency\":\"USD\",\"amount\":80.0}}",
                        MediaType.APPLICATION_JSON
                ));

        BakongGatewayService gateway = new BakongGatewayService(props, builder.build());

        BakongGatewayService.KhqrGenerationResult qrResult = gateway.createDynamicPayment(
                BigDecimal.valueOf(80.00),
                "BK-INV001",
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );

        assertNotNull(qrResult.qrCode());
        assertTrue(qrResult.qrCode().startsWith("000201"), "Valid EMVCo KHQR string must start with 000201");
        assertNotNull(qrResult.md5());
        assertEquals(32, qrResult.md5().length());
        assertEquals("https://bakong.page.link/abc", qrResult.deepLink());
        assertEquals("USD", qrResult.currency());

        var decoded = kh.gov.nbc.bakong_khqr.BakongKHQR.decode(qrResult.qrCode());
        assertNotNull(decoded.getData());
        assertEquals("lumen_account@devb", decoded.getData().getBakongAccountID());
        assertNull(decoded.getData().getMerchantId(), "Individual KHQR should not require or embed a Merchant ID");

        BakongGatewayService.BakongTransactionCheckResult pendingCheck = gateway.checkTransactionByMd5(qrResult.md5());
        assertEquals(BakongGatewayService.TransactionLookupState.PENDING, pendingCheck.state());

        BakongGatewayService.BakongTransactionCheckResult paidCheck = gateway.checkTransactionByMd5(qrResult.md5());
        assertEquals(BakongGatewayService.TransactionLookupState.PAID, paidCheck.state());
        assertEquals("txhash123", paidCheck.hash());
        assertEquals("payer@aba", paidCheck.fromAccountId());
        assertEquals(0, BigDecimal.valueOf(80.00).compareTo(paidCheck.amount()));

        server.verify();
    }

    @Test
    @DisplayName("BakongGatewayService keeps QR working when deeplink endpoint returns HTTP error")
    void testDeepLinkFailureIsNonFatal() {
        BakongProperties props = new BakongProperties();
        props.getApi().setBaseUrl("https://sit-api-bakong.nbc.gov.kh");
        props.getApi().setToken("fake-sit-jwt-token");
        props.setAccountId("lumen_account@devb");

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://sit-api-bakong.nbc.gov.kh/v1/generate_deeplink_by_qr"))
                .andRespond(withServerError());

        BakongGatewayService gateway = new BakongGatewayService(props, builder.build());
        BakongGatewayService.KhqrGenerationResult res = gateway.createDynamicPayment(
                BigDecimal.valueOf(25.00),
                "BK-FALLBACK",
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );

        assertNotNull(res.qrCode());
        assertNotNull(res.md5());
        assertNull(res.deepLink());
        server.verify();
    }

    @Test
    @DisplayName("PaymentService generates Bakong KHQR with 50% discount for expired enrollment and verifies payment -> enroll")
    void testCheckoutVerifyAndEnrollFlow() {
        BakongGatewayService mockGateway = Mockito.mock(BakongGatewayService.class);
        when(mockGateway.isConfigured()).thenReturn(true);
        when(mockGateway.getQrExpirationMinutes()).thenReturn(15);
        when(mockGateway.getConfiguredCurrency()).thenReturn("USD");
        when(mockGateway.getConfiguredAccountId()).thenReturn("lumen@aba");

        when(mockGateway.createDynamicPayment(eq(BigDecimal.valueOf(40.00).setScale(2)), any(), any()))
                .thenReturn(new BakongGatewayService.KhqrGenerationResult(
                        "000201MOCKKHQR",
                        "0123456789abcdef0123456789abcdef",
                        "https://bakong.page.link/mock",
                        "USD"
                ));

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                courseRepository,
                userRepository,
                enrollmentService,
                new PaymentMapper(),
                emailService,
                notificationService,
                enrollmentRepository,
                mockGateway
        );

        when(userRepository.findByEmail("sokha@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(5L)).thenReturn(Optional.of(paidCourse));

        Enrollments expired = new Enrollments();
        expired.setUser(student);
        expired.setCourse(paidCourse);
        expired.setStatus(EnrollmentStatus.EXPIRED);
        expired.setExpirationDate(Timestamp.from(Instant.now().minus(5, ChronoUnit.DAYS)));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(10L, 5L)).thenReturn(Optional.of(expired));
        when(paymentRepository.findFirstByUser_EmailAndCourse_CourseIdAndProviderAndStatusOrderByCreatedAtDesc(
                "sokha@example.com", 5L, PaymentService.PROVIDER_BAKONG_KHQR, PaymentStatus.PENDING))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payments.class))).thenAnswer(inv -> {
            Payments p = inv.getArgument(0);
            if (p.getPaymentId() == null) p.setPaymentId(301L);
            if (p.getCreatedAt() == null) p.setCreatedAt(Timestamp.from(Instant.now()));
            return p;
        });

        PaymentResponseDTO checkoutRes = paymentService.createCheckout(
                new PaymentCheckoutRequestDTO(5L, "BAKONG_KHQR"),
                "sokha@example.com"
        );

        assertEquals(301L, checkoutRes.paymentId());
        assertEquals(PaymentStatus.PENDING, checkoutRes.status());
        assertEquals("BAKONG_KHQR", checkoutRes.provider());
        assertEquals("000201MOCKKHQR", checkoutRes.qrCode());
        assertEquals("0123456789abcdef0123456789abcdef", checkoutRes.md5());
        assertEquals("https://bakong.page.link/mock", checkoutRes.deepLink());
        assertEquals(0, BigDecimal.valueOf(40.00).compareTo(checkoutRes.amount()));
        assertTrue(checkoutRes.isReEnrollmentDiscount());

        // Simulate status verification when Bakong reports PAID
        Payments savedPending = new Payments();
        savedPending.setPaymentId(301L);
        savedPending.setUser(student);
        savedPending.setCourse(paidCourse);
        savedPending.setAmount(BigDecimal.valueOf(40.00));
        savedPending.setOriginalAmount(BigDecimal.valueOf(80.00));
        savedPending.setDiscountAmount(BigDecimal.valueOf(40.00));
        savedPending.setIsReEnrollmentDiscount(true);
        savedPending.setProvider("BAKONG_KHQR");
        savedPending.setProviderReference("BK-123");
        savedPending.setKhqrPayload("000201MOCKKHQR");
        savedPending.setKhqrMd5("0123456789abcdef0123456789abcdef");
        savedPending.setCurrency("USD");
        savedPending.setExpiresAt(Timestamp.from(Instant.now().plus(14, ChronoUnit.MINUTES)));
        savedPending.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByIdForUpdate(301L)).thenReturn(Optional.of(savedPending));
        when(mockGateway.checkTransactionByMd5("0123456789abcdef0123456789abcdef")).thenReturn(
                new BakongGatewayService.BakongTransactionCheckResult(
                        BakongGatewayService.TransactionLookupState.PAID,
                        "bakong_tx_hash_999",
                        "sokha@acleda",
                        "lumen@aba",
                        "USD",
                        BigDecimal.valueOf(40.00),
                        "Success"
                )
        );

        PaymentResponseDTO statusRes = paymentService.checkAndSyncPaymentStatus(301L, "sokha@example.com", false);

        assertEquals(PaymentStatus.PAID, statusRes.status());
        assertEquals("bakong_tx_hash_999", statusRes.providerReference());
        assertNotNull(statusRes.paidAt());
        verify(enrollmentService).enrollCurrentUser(eq(new EnrollmentCreateDTO(5L)), eq("sokha@example.com"));
        verify(notificationService).sendNotification(eq(student), eq("Payment Confirmed"), anyString(), eq("PAYMENT"), anyString());
    }

    @Test
    @DisplayName("PaymentService rejects underpaid Bakong transaction and blocks non-owner access")
    void testUnderpaymentAndRbacProtection() {
        BakongGatewayService mockGateway = Mockito.mock(BakongGatewayService.class);
        when(mockGateway.isConfigured()).thenReturn(true);
        when(mockGateway.getConfiguredCurrency()).thenReturn("USD");
        when(mockGateway.getConfiguredAccountId()).thenReturn("lumen@aba");

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                courseRepository,
                userRepository,
                enrollmentService,
                new PaymentMapper(),
                emailService,
                notificationService,
                enrollmentRepository,
                mockGateway
        );

        Payments pending = new Payments();
        pending.setPaymentId(401L);
        pending.setUser(student);
        pending.setCourse(paidCourse);
        pending.setAmount(BigDecimal.valueOf(80.00));
        pending.setProvider("BAKONG_KHQR");
        pending.setKhqrMd5("abcdef0123456789abcdef0123456789");
        pending.setCurrency("USD");
        pending.setExpiresAt(Timestamp.from(Instant.now().plus(10, ChronoUnit.MINUTES)));
        pending.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByIdForUpdate(401L)).thenReturn(Optional.of(pending));

        // Another student cannot check status
        ResponseStatusException forbiddenEx = assertThrows(ResponseStatusException.class, () ->
                paymentService.checkAndSyncPaymentStatus(401L, "dara@example.com", false)
        );
        assertEquals(HttpStatus.FORBIDDEN, forbiddenEx.getStatusCode());

        // Underpayment ($1 instead of $80) must NOT mark PAID or enroll
        when(mockGateway.checkTransactionByMd5("abcdef0123456789abcdef0123456789")).thenReturn(
                new BakongGatewayService.BakongTransactionCheckResult(
                        BakongGatewayService.TransactionLookupState.PAID,
                        "underpaid_hash",
                        "attacker@aba",
                        "lumen@aba",
                        "USD",
                        BigDecimal.valueOf(1.00),
                        "Success"
                )
        );

        PaymentResponseDTO adminCheck = paymentService.checkAndSyncPaymentStatus(401L, "admin@example.com", true);
        assertEquals(PaymentStatus.PENDING, adminCheck.status());
        verify(enrollmentService, never()).enrollCurrentUser(any(), any());
    }

    @Test
    @DisplayName("PaymentController GET /api/payments/{id}/status returns 200 with Bakong status and blocks active enrollment checkout")
    void testControllerStatusEndpointAndActiveEnrollmentConflict() throws Exception {
        BakongGatewayService mockGateway = Mockito.mock(BakongGatewayService.class);
        when(mockGateway.isConfigured()).thenReturn(true);

        PaymentService paymentService = new PaymentService(
                paymentRepository,
                courseRepository,
                userRepository,
                enrollmentService,
                new PaymentMapper(),
                emailService,
                notificationService,
                enrollmentRepository,
                mockGateway
        );

        when(userRepository.findByEmail("sokha@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(5L)).thenReturn(Optional.of(paidCourse));

        Enrollments active = new Enrollments();
        active.setUser(student);
        active.setCourse(paidCourse);
        active.setStatus(EnrollmentStatus.ACTIVE);
        active.setExpirationDate(Timestamp.from(Instant.now().plus(90, ChronoUnit.DAYS)));
        when(enrollmentRepository.findByUser_IdAndCourse_CourseId(10L, 5L)).thenReturn(Optional.of(active));

        ResponseStatusException conflictEx = assertThrows(ResponseStatusException.class, () ->
                paymentService.createCheckout(new PaymentCheckoutRequestDTO(5L, "BAKONG_KHQR"), "sokha@example.com")
        );
        assertEquals(HttpStatus.CONFLICT, conflictEx.getStatusCode());

        // Expired unpaid payment should transition to EXPIRED via GET /api/payments/{id}/status
        Payments overdue = new Payments();
        overdue.setPaymentId(501L);
        overdue.setUser(student);
        overdue.setCourse(paidCourse);
        overdue.setAmount(BigDecimal.valueOf(80.00));
        overdue.setProvider("BAKONG_KHQR");
        overdue.setKhqrMd5("expiredmd50000000000000000000000");
        overdue.setKhqrPayload("000201EXPIRED");
        overdue.setCurrency("USD");
        overdue.setExpiresAt(Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
        overdue.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByIdForUpdate(501L)).thenReturn(Optional.of(overdue));
        when(paymentRepository.save(any(Payments.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockGateway.checkTransactionByMd5("expiredmd50000000000000000000000")).thenReturn(
                new BakongGatewayService.BakongTransactionCheckResult(
                        BakongGatewayService.TransactionLookupState.PENDING,
                        null, null, null, null, null, "Not found"
                )
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService)).build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "sokha@example.com",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        mockMvc.perform(get("/api/payments/501/status").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is(501)))
                .andExpect(jsonPath("$.status", is("EXPIRED")))
                .andExpect(jsonPath("$.provider", is("BAKONG_KHQR")));

        // Verify scheduler invokes reconciliation per pending Bakong invoice
        PaymentService spiedPaymentService = Mockito.mock(PaymentService.class);
        when(paymentRepository.findByProviderAndStatusAndKhqrMd5IsNotNull("BAKONG_KHQR", PaymentStatus.PENDING))
                .thenReturn(List.of(overdue));
        PaymentReconciliationScheduler scheduler = new PaymentReconciliationScheduler(paymentRepository, spiedPaymentService);
        scheduler.reconcilePendingBakongPayments();
        verify(spiedPaymentService).reconcilePendingPaymentById(501L);
    }
}
