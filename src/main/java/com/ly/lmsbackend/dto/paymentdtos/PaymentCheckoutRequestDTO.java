package com.ly.lmsbackend.dto.paymentdtos;

import jakarta.validation.constraints.NotNull;

public record PaymentCheckoutRequestDTO(
        @NotNull Long courseId,
        String provider
) {
}
