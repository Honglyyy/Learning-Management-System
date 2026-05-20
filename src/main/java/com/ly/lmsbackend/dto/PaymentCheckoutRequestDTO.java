package com.ly.lmsbackend.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentCheckoutRequestDTO(
        @NotNull Long courseId,
        String provider
) {
}
