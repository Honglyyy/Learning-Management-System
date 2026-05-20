package com.ly.lmsbackend.dto;

import com.ly.lmsbackend.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentStatusUpdateDTO(
        @NotNull PaymentStatus status,
        String providerReference
) {
}
