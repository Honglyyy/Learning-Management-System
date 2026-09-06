package com.ly.lmsbackend.dto.instructordtos;

import jakarta.validation.constraints.NotBlank;

public record InstructorProfileUpdateDTO(
        @NotBlank(message = "Full name is required")
        String fullName,
        String phoneNumber,
        String biography,
        String expertise,
        String profilePhotoUrl,
        String profilePhotoPublicId
) {
}
