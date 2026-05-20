package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.PaymentCheckoutRequestDTO;
import com.ly.lmsbackend.dto.PaymentResponseDTO;
import com.ly.lmsbackend.mapper.PaymentMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.PaymentStatus;
import com.ly.lmsbackend.model.Payments;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.PaymentRepository;
import com.ly.lmsbackend.repository.UserRepository;
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

    public PaymentService(
            PaymentRepository paymentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            EnrollmentService enrollmentService,
            PaymentMapper paymentMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
        this.paymentMapper = paymentMapper;
    }

    public PaymentResponseDTO createCheckout(PaymentCheckoutRequestDTO dto, String email) {
        Users user = getUser(email);
        Courses course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Payments payment = new Payments();
        payment.setUser(user);
        payment.setCourse(course);
        payment.setAmount(course.getPrice() == null ? BigDecimal.ZERO : course.getPrice());
        payment.setProvider(dto.provider() == null || dto.provider().isBlank() ? "MANUAL" : dto.provider());
        payment.setProviderReference("checkout_" + UUID.randomUUID());
        payment.setStatus(PaymentStatus.PENDING);

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    public PaymentResponseDTO confirmPayment(Long paymentId, String email) {
        Payments payment = getPayment(paymentId);
        if (!payment.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot confirm this payment");
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            return paymentMapper.toDTO(payment);
        }

        payment.setStatus(PaymentStatus.PAID);
        Payments saved = paymentRepository.save(payment);
        enrollIfNeeded(saved, email);

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
}
