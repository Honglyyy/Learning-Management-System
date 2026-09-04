package com.ly.lmsbackend.dto.authdtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyUserOtp(
        @NotBlank @Email String email,
        @NotBlank String otp
) {
}
