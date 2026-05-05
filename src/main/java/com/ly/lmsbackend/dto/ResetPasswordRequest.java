package com.ly.lmsbackend.dto;

public record ResetPasswordRequest(
        String email,
        String password,
        String otp
) {
}
