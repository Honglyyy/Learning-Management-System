package com.ly.lmsbackend.dto.authdtos;

import com.ly.lmsbackend.model.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotBlank String fullName,
        String phoneNumber,
        Roles role
) {
}
