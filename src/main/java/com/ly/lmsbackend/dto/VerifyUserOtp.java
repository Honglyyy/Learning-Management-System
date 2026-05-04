package com.ly.lmsbackend.dto;

import org.antlr.v4.runtime.misc.NotNull;

public record VerifyUserOtp(
        String email,
        String otp
) {
}
