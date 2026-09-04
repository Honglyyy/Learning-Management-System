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
        Timestamp updatedAt
) {
}
