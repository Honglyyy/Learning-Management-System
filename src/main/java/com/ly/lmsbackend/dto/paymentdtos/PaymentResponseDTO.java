package com.ly.lmsbackend.dto.paymentdtos;

import com.ly.lmsbackend.model.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record PaymentResponseDTO(
        Long paymentId,
        Long userId,
        String username,
        String userEmail,
        Long courseId,
        String courseTitle,
        BigDecimal amount,
        String provider,
        String providerReference,
        PaymentStatus status,
        Timestamp createdAt,
        Timestamp updatedAt,
        String paymentUrl,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        Boolean isReEnrollmentDiscount
) {
    public PaymentResponseDTO(
            Long paymentId,
            Long userId,
            String username,
            String userEmail,
            Long courseId,
            String courseTitle,
            BigDecimal amount,
            String provider,
            String providerReference,
            PaymentStatus status,
            Timestamp createdAt,
            Timestamp updatedAt
    ) {
        this(paymentId, userId, username, userEmail, courseId, courseTitle, amount, provider, providerReference, status, createdAt, updatedAt, null, amount, BigDecimal.ZERO, false);
    }

    public PaymentResponseDTO(
            Long paymentId,
            Long userId,
            String username,
            String userEmail,
            Long courseId,
            String courseTitle,
            BigDecimal amount,
            String provider,
            String providerReference,
            PaymentStatus status,
            Timestamp createdAt,
            Timestamp updatedAt,
            String paymentUrl
    ) {
        this(paymentId, userId, username, userEmail, courseId, courseTitle, amount, provider, providerReference, status, createdAt, updatedAt, paymentUrl, amount, BigDecimal.ZERO, false);
    }
}
