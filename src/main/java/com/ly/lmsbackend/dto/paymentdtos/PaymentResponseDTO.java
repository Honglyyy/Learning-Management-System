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
        Boolean isReEnrollmentDiscount,
        String qrCode,
        String md5,
        String deepLink,
        String currency,
        Timestamp expiresAt,
        Timestamp paidAt
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
        this(paymentId, userId, username, userEmail, courseId, courseTitle, amount, provider, providerReference, status, createdAt, updatedAt, null, amount, BigDecimal.ZERO, false, null, null, null, "USD", null, null);
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
        this(paymentId, userId, username, userEmail, courseId, courseTitle, amount, provider, providerReference, status, createdAt, updatedAt, paymentUrl, amount, BigDecimal.ZERO, false, null, null, null, "USD", null, null);
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
            String paymentUrl,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            Boolean isReEnrollmentDiscount
    ) {
        this(paymentId, userId, username, userEmail, courseId, courseTitle, amount, provider, providerReference, status, createdAt, updatedAt, paymentUrl, originalAmount, discountAmount, isReEnrollmentDiscount, null, null, null, "USD", null, null);
    }
}
