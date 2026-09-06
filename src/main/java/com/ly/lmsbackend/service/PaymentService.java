package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentCheckoutRequestDTO;
import com.ly.lmsbackend.dto.paymentdtos.PaymentResponseDTO;
import com.ly.lmsbackend.mapper.PaymentMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.PaymentRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;
    private final PaymentMapper paymentMapper;
    private final EmailService emailService;

    @Autowired
    public PaymentService(
            PaymentRepository paymentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            PaymentMapper paymentMapper,
            EmailService emailService
    ) {
        this.paymentRepository = paymentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.paymentMapper = paymentMapper;
        this.emailService = emailService;
    }

    public PaymentService(
            PaymentRepository paymentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            PaymentMapper paymentMapper
    ) {
        this(paymentRepository, courseRepository, userRepository, enrollmentService, paymentMapper, null);
    }

    public PaymentResponseDTO createCheckout(PaymentCheckoutRequestDTO dto, String email) {
        Users user = getUser(email);
        Courses course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Payments payment = new Payments();
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmount(course.getPrice() == null ? BigDecimal.ZERO : course.getPrice());
        payment.setProvider(dto.provider() == null || dto.provider().isBlank() ? "ABA_PAYWAY" : dto.provider());
        payment.setProviderReference("aba_" + UUID.randomUUID().toString().substring(0, 8));
        payment.setStatus(PaymentStatus.PENDING);

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    public PaymentResponseDTO confirmPayment(Long paymentId, String email) {
        Payments payment = getPayment(paymentId);
        Users user = getUser(email);

        boolean isAdmin = user.getRole() == Roles.ADMIN;
        boolean isOwner = payment.getUser().getEmail().equals(email);

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to confirm this payment");
        }

        // Paid courses must be confirmed by an admin after the student pays via ABA PayWay
        if (!isAdmin && payment.getAmount() != null && payment.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Paid courses require admin confirmation after completing payment via ABA PayWay");
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            return paymentMapper.toDTO(payment);
        }

        payment.setStatus(PaymentStatus.PAID);
        Payments saved = paymentRepository.save(payment);
        enrollIfNeeded(saved, saved.getUser().getEmail());
        notifyStudentAbaPaymentApproved(saved);

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

    public PaymentResponseDTO updateStatus(Long paymentId, PaymentStatus status, String providerReference) {
        Payments payment = getPayment(paymentId);
        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(status);
        if (providerReference != null && !providerReference.isBlank()) {
            payment.setProviderReference(providerReference);
        }

        Payments saved = paymentRepository.save(payment);
        if (status == PaymentStatus.PAID && previousStatus != PaymentStatus.PAID) {
            enrollIfNeeded(saved, saved.getUser().getEmail());
            notifyStudentAbaPaymentApproved(saved);
        }

        return paymentMapper.toDTO(saved);
    }

    public void deletePayment(Long paymentId) {
        paymentRepository.deleteById(paymentId);
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

    private void notifyStudentAbaPaymentApproved(Payments payment) {
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
            String txnRef = payment.getProviderReference() != null ? payment.getProviderReference() : "ABA-" + payment.getPaymentId();
            String enrollmentDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date());
            Long courseId = course != null ? course.getCourseId() : null;
            String courseUrl = courseId != null ? "http://localhost:5173/courses/" + courseId : "http://localhost:5173/my/enrollments";

            emailService.sendAbaPayWayEnrollmentSuccess(
                    student.getEmail(),
                    studentName,
                    courseTitle,
                    instructorName,
                    amount,
                    txnRef,
                    enrollmentDate,
                    courseUrl
            );
        } catch (Exception ignored) {
        }
    }
}
