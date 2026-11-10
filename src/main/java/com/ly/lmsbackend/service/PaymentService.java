package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.paymentdtos.CheckoutQuoteDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentCheckoutRequestDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentResponseDTO;
import com.ly.lmsbackend.mapper.PaymentMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.EnrollmentStatus;
import com.ly.lmsbackend.model.Enrollments;
import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.EnrollmentRepository;
import com.ly.lmsbackend.repository.PaymentRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    public static final String PROVIDER_BAKONG_KHQR = "BAKONG_KHQR";
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final PaymentMapper paymentMapper;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final EnrollmentRepository enrollmentRepository;
    private final BakongGatewayService bakongGatewayService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl = "http://localhost:5173";

    @Autowired
    public PaymentService(
            PaymentRepository paymentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            PaymentMapper paymentMapper,
            EmailService emailService,
            NotificationService notificationService,
            EnrollmentRepository enrollmentRepository,
            BakongGatewayService bakongGatewayService
    ) {
        this.paymentRepository = paymentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.paymentMapper = paymentMapper;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.enrollmentRepository = enrollmentRepository;
        this.bakongGatewayService = bakongGatewayService;
    }

    public PaymentService(
            PaymentRepository paymentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            PaymentMapper paymentMapper,
            EmailService emailService,
            NotificationService notificationService,
            EnrollmentRepository enrollmentRepository
    ) {
        this(paymentRepository, courseRepository, userRepository, enrollmentService, paymentMapper, emailService, notificationService, enrollmentRepository, null);
    }

    public PaymentService(
            PaymentRepository paymentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            PaymentMapper paymentMapper,
            EmailService emailService,
            NotificationService notificationService
    ) {
        this(paymentRepository, courseRepository, userRepository, enrollmentService, paymentMapper, emailService, notificationService, null, null);
    }

    public PaymentService(
            PaymentRepository paymentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            PaymentMapper paymentMapper
    ) {
        this(paymentRepository, courseRepository, userRepository, enrollmentService, paymentMapper, null, null, null, null);
    }

    @Transactional
    public PaymentResponseDTO createCheckout(PaymentCheckoutRequestDTO dto, String email) {
        Users user = getUser(email);
        Courses course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        BigDecimal basePrice = course.getPrice() == null ? BigDecimal.ZERO : course.getPrice();
        BigDecimal finalAmount = basePrice;
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean hasReEnrollmentDiscount = false;
        Optional<Enrollments> existingEnrollmentOpt = Optional.empty();

        if (enrollmentRepository != null) {
            existingEnrollmentOpt = enrollmentRepository
                    .findByUser_IdAndCourse_CourseId(user.getId(), course.getCourseId());

            if (existingEnrollmentOpt.isPresent() && existingEnrollmentOpt.get().isExpired()) {
                hasReEnrollmentDiscount = true;
                discountAmount = basePrice.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
                finalAmount = basePrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
            }
        }

        String requestedProvider = dto.provider() == null ? null : dto.provider().trim();
        boolean isBakongRequested = isBakongProvider(requestedProvider);

        if (isBakongRequested && finalAmount.compareTo(BigDecimal.ZERO) > 0) {
            return createBakongCheckout(user, course, basePrice, finalAmount, discountAmount, hasReEnrollmentDiscount, existingEnrollmentOpt);
        }

        String resolvedProvider = (requestedProvider == null || requestedProvider.isBlank())
                ? (finalAmount.compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "UNASSIGNED")
                : requestedProvider;
        String refPrefix = finalAmount.compareTo(BigDecimal.ZERO) == 0 ? "free_" : "txn_";

        Payments payment = new Payments();
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmount(finalAmount);
        payment.setOriginalAmount(basePrice);
        payment.setDiscountAmount(discountAmount);
        payment.setIsReEnrollmentDiscount(hasReEnrollmentDiscount);
        payment.setProvider(resolvedProvider);
        payment.setProviderReference(refPrefix + UUID.randomUUID().toString().substring(0, 8));
        payment.setStatus(PaymentStatus.PENDING);

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    private PaymentResponseDTO createBakongCheckout(
            Users user,
            Courses course,
            BigDecimal basePrice,
            BigDecimal finalAmount,
            BigDecimal discountAmount,
            boolean hasReEnrollmentDiscount,
            Optional<Enrollments> existingEnrollmentOpt
    ) {
        if (existingEnrollmentOpt.isPresent()) {
            Enrollments existing = existingEnrollmentOpt.get();
            if (!existing.isExpired() && existing.getStatus() != EnrollmentStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already actively enrolled in this course");
            }
        }

        if (bakongGatewayService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Bakong payment gateway service is unavailable");
        }
        bakongGatewayService.ensureConfigured();

        Optional<Payments> pendingOpt = paymentRepository
                .findFirstByUser_EmailAndCourse_CourseIdAndProviderAndStatusOrderByCreatedAtDesc(
                        user.getEmail(),
                        course.getCourseId(),
                        PROVIDER_BAKONG_KHQR,
                        PaymentStatus.PENDING
                );

        if (pendingOpt.isPresent()) {
            Payments existingPending = pendingOpt.get();
            // First check if the prior pending invoice was already paid before issuing a duplicate QR
            if (existingPending.getKhqrMd5() != null && !existingPending.getKhqrMd5().isBlank()) {
                Payments checked = verifyBakongPaymentInternal(existingPending, false);
                if (checked.getStatus() == PaymentStatus.PAID) {
                    return paymentMapper.toDTO(checked);
                }
            }

            if (isPendingBakongReusable(existingPending, finalAmount, existingEnrollmentOpt)) {
                return paymentMapper.toDTO(existingPending);
            }

            existingPending.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(existingPending);
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(bakongGatewayService.getQrExpirationMinutes(), ChronoUnit.MINUTES);
        String reference = "BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        BakongGatewayService.KhqrGenerationResult khqr =
                bakongGatewayService.createDynamicPayment(finalAmount, reference, expiresAt);

        Payments payment = new Payments();
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmount(finalAmount);
        payment.setOriginalAmount(basePrice);
        payment.setDiscountAmount(discountAmount);
        payment.setIsReEnrollmentDiscount(hasReEnrollmentDiscount);
        payment.setProvider(PROVIDER_BAKONG_KHQR);
        payment.setProviderReference(reference);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setKhqrPayload(khqr.qrCode());
        payment.setKhqrMd5(khqr.md5());
        payment.setBakongDeepLink(khqr.deepLink());
        payment.setCurrency(khqr.currency());
        payment.setExpiresAt(Timestamp.from(expiresAt));

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    private boolean isPendingBakongReusable(
            Payments existing,
            BigDecimal expectedAmount,
            Optional<Enrollments> existingEnrollmentOpt
    ) {
        if (existing.getStatus() != PaymentStatus.PENDING) {
            return false;
        }
        if (existing.getKhqrPayload() == null || existing.getKhqrPayload().isBlank()
                || existing.getKhqrMd5() == null || existing.getKhqrMd5().isBlank()) {
            return false;
        }
        if (existing.getExpiresAt() == null
                || !existing.getExpiresAt().toInstant().isAfter(Instant.now().plusSeconds(30))) {
            return false;
        }
        if (existing.getAmount() == null
                || existing.getAmount().setScale(2, RoundingMode.HALF_UP).compareTo(expectedAmount.setScale(2, RoundingMode.HALF_UP)) != 0) {
            return false;
        }
        if (existingEnrollmentOpt.isPresent() && existingEnrollmentOpt.get().isExpired()) {
            Timestamp cutoff = existingEnrollmentOpt.get().getExpirationDate();
            if (cutoff != null && existing.getCreatedAt() != null && existing.getCreatedAt().before(cutoff)) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public PaymentResponseDTO checkAndSyncPaymentStatus(Long paymentId, String requesterEmail, boolean isAdmin) {
        Payments payment = paymentRepository.findByIdForUpdate(paymentId)
                .or(() -> paymentRepository.findById(paymentId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        boolean isOwner = payment.getUser() != null && requesterEmail.equals(payment.getUser().getEmail());
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view or verify this payment");
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            enrollIfNeeded(payment, payment.getUser().getEmail());
            return paymentMapper.toDTO(payment);
        }

        if (payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.REFUNDED
                || payment.getStatus() == PaymentStatus.EXPIRED) {
            return paymentMapper.toDTO(payment);
        }

        if (isBakongProvider(payment.getProvider()) && payment.getKhqrMd5() != null && !payment.getKhqrMd5().isBlank()) {
            if (bakongGatewayService == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Bakong payment gateway service is unavailable");
            }
            bakongGatewayService.ensureConfigured();
            Payments updated = verifyBakongPaymentInternal(payment, true);
            return paymentMapper.toDTO(updated);
        }

        return paymentMapper.toDTO(payment);
    }

    @Transactional
    public void reconcilePendingPaymentById(Long paymentId) {
        Optional<Payments> opt = paymentRepository.findByIdForUpdate(paymentId)
                .or(() -> paymentRepository.findById(paymentId));
        if (opt.isEmpty()) {
            return;
        }
        Payments payment = opt.get();
        if (payment.getStatus() != PaymentStatus.PENDING || !isBakongProvider(payment.getProvider())) {
            return;
        }

        if (bakongGatewayService != null && bakongGatewayService.isConfigured()
                && payment.getKhqrMd5() != null && !payment.getKhqrMd5().isBlank()) {
            verifyBakongPaymentInternal(payment, true);
        } else if (payment.getExpiresAt() != null && payment.getExpiresAt().toInstant().isBefore(Instant.now())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
        }
    }

    private Payments verifyBakongPaymentInternal(Payments payment, boolean expireIfOverdue) {
        if (bakongGatewayService == null || !bakongGatewayService.isConfigured() || payment.getKhqrMd5() == null) {
            return payment;
        }

        BakongGatewayService.BakongTransactionCheckResult tx = bakongGatewayService.checkTransactionByMd5(payment.getKhqrMd5());

        if (tx.state() == BakongGatewayService.TransactionLookupState.PAID) {
            if (!isTransactionValidForPayment(payment, tx)) {
                log.warn("Bakong transaction verification mismatch for paymentId={}: txAmount={}, txCurrency={}, txTo={}",
                        payment.getPaymentId(), tx.amount(), tx.currency(), tx.toAccountId());
                return payment;
            }

            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(Timestamp.from(Instant.now()));
            payment.setBakongTransactionHash(tx.hash());
            payment.setPayerAccountId(tx.fromAccountId());
            if (tx.hash() != null && !tx.hash().isBlank()) {
                payment.setProviderReference(tx.hash());
            }

            Payments saved = paymentRepository.save(payment);
            enrollIfNeeded(saved, saved.getUser().getEmail());
            notifyStudentPaymentConfirmed(saved);
            return saved;
        }

        if (expireIfOverdue
                && tx.state() == BakongGatewayService.TransactionLookupState.PENDING
                && payment.getExpiresAt() != null
                && payment.getExpiresAt().toInstant().isBefore(Instant.now())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            return paymentRepository.save(payment);
        }

        return payment;
    }

    private boolean isTransactionValidForPayment(Payments payment, BakongGatewayService.BakongTransactionCheckResult tx) {
        String expectedCurrency = payment.getCurrency() != null && !payment.getCurrency().isBlank()
                ? payment.getCurrency().trim()
                : bakongGatewayService.getConfiguredCurrency();
        if (tx.currency() != null && !tx.currency().isBlank()
                && !expectedCurrency.equalsIgnoreCase(tx.currency().trim())) {
            return false;
        }

        if (tx.amount() == null || payment.getAmount() == null) {
            return false;
        }
        BigDecimal expectedAmount = payment.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal paidAmount = tx.amount().setScale(2, RoundingMode.HALF_UP);
        if (paidAmount.compareTo(expectedAmount) < 0) {
            return false;
        }

        String configuredAccount = bakongGatewayService.getConfiguredAccountId();
        if (tx.toAccountId() != null && !tx.toAccountId().isBlank()
                && !configuredAccount.isBlank()
                && !configuredAccount.equalsIgnoreCase(tx.toAccountId().trim())) {
            return false;
        }

        return true;
    }

    public CheckoutQuoteDTO calculateCheckoutQuote(Long courseId, String email) {
        Users user = getUser(email);
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        BigDecimal basePrice = course.getPrice() == null ? BigDecimal.ZERO : course.getPrice();
        BigDecimal finalPrice = basePrice;
        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean hasReEnrollmentDiscount = false;
        Double discountPercentage = 0.0;
        boolean isExpired = false;

        if (enrollmentRepository != null) {
            Optional<Enrollments> existingEnrollmentOpt = enrollmentRepository
                    .findByUser_IdAndCourse_CourseId(user.getId(), course.getCourseId());

            if (existingEnrollmentOpt.isPresent() && existingEnrollmentOpt.get().isExpired()) {
                isExpired = true;
                if (basePrice.compareTo(BigDecimal.ZERO) > 0) {
                    hasReEnrollmentDiscount = true;
                    discountPercentage = 50.0;
                    discountAmount = basePrice.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
                    finalPrice = basePrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }

        return new CheckoutQuoteDTO(
                course.getCourseId(),
                course.getTitle(),
                basePrice,
                finalPrice,
                discountAmount,
                discountPercentage,
                hasReEnrollmentDiscount,
                course.getAccessDurationDays(),
                isExpired
        );
    }

    public PaymentResponseDTO confirmPayment(Long paymentId, String email) {
        Payments payment = getPayment(paymentId);

        boolean isOwner = payment.getUser() != null && email.equals(payment.getUser().getEmail());
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to confirm this payment");
        }

        // Paid courses must be verified directly against the bank/gateway response (e.g. Bakong)
        if (payment.getAmount() != null && payment.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Paid courses require automated bank payment verification and cannot be manually confirmed"
            );
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            return paymentMapper.toDTO(payment);
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Timestamp.from(Instant.now()));
        Payments saved = paymentRepository.save(payment);
        enrollIfNeeded(saved, saved.getUser().getEmail());
        notifyStudentPaymentConfirmed(saved);

        return paymentMapper.toDTO(saved);
    }

    public List<PaymentResponseDTO> getMyPayments(String email) {
        return paymentRepository.findByUser_Email(email)
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    public void deletePayment(Long paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    private static boolean isBakongProvider(String provider) {
        if (provider == null) return false;
        String trimmed = provider.trim();
        return PROVIDER_BAKONG_KHQR.equalsIgnoreCase(trimmed) || "BAKONG".equalsIgnoreCase(trimmed);
    }

    private Payments getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void enrollIfNeeded(Payments payment, String email) {
        try {
            enrollmentService.enrollCurrentUser(new EnrollmentCreateDTO(payment.getCourse().getCourseId()), email);
        } catch (ResponseStatusException exception) {
            if (!exception.getStatusCode().equals(HttpStatus.CONFLICT)) {
                throw exception;
            }
        }
    }

    protected void notifyStudentPaymentConfirmed(Payments payment) {
        if (emailService == null || payment == null || payment.getUser() == null) {
            return;
        }
        try {
            Users student = payment.getUser();
            Courses course = payment.getCourse();
            String studentName = (student.getStudent() != null && student.getStudent().getFullName() != null)
                    ? student.getStudent().getFullName()
                    : (student.getFullname() != null ? student.getFullname() : student.getUsername());

            String courseTitle = course != null ? course.getTitle() : "Course";
            String instructorName = "Lumen Instructor";
            if (course != null && course.getInstructor() != null) {
                Users inst = course.getInstructor();
                instructorName = (inst.getInstructor() != null && inst.getInstructor().getFullName() != null)
                        ? inst.getInstructor().getFullName()
                        : (inst.getFullname() != null ? inst.getFullname() : inst.getUsername());
            }

            String amount = payment.getAmount() != null ? payment.getAmount().toPlainString() : "0.00";
            String paymentMethod = (payment.getProvider() != null && !payment.getProvider().isBlank())
                    ? payment.getProvider()
                    : "Online Payment";
            String txnRef = payment.getProviderReference() != null ? payment.getProviderReference() : "PAY-" + payment.getPaymentId();
            String enrollmentDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
            Long courseId = course != null ? course.getCourseId() : null;
            String baseFrontend = (frontendUrl == null || frontendUrl.isBlank())
                    ? "http://localhost:5173"
                    : (frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl.trim());
            String courseUrl = courseId != null ? baseFrontend + "/courses/" + courseId : baseFrontend + "/my/enrollments";

            emailService.sendPaymentEnrollmentSuccess(
                    student.getEmail(),
                    studentName,
                    courseTitle,
                    instructorName,
                    amount,
                    paymentMethod,
                    txnRef,
                    enrollmentDate,
                    courseUrl
            );

            if (notificationService != null) {
                notificationService.sendNotification(
                        student,
                        "Payment Confirmed",
                        "Your payment for '" + courseTitle + "' has been confirmed! You can now access the course.",
                        "PAYMENT",
                        courseUrl
                );
            }
        } catch (Exception ignored) {
        }
    }
}
