package com.ly.lmsbackend.dto.studentdtos;

import com.ly.lmsbackend.model.Genders;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record StudentProfileUpdateDTO(
        @NotBlank(message = "Full name is required")
        String fullName,
        String phoneNumber,
        Genders gender,
        LocalDate dateOfBirth,
        String educationLevel,
        String profilePhotoUrl,
        String profilePhotoPublicId
) {
}
