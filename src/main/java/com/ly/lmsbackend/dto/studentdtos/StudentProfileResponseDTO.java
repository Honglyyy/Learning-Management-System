package com.ly.lmsbackend.dto.studentdtos;

import com.ly.lmsbackend.model.Genders;
import lombok.Builder;

import java.sql.Timestamp;
import java.time.LocalDate;

@Builder
public record StudentProfileResponseDTO(
        Long studentId,
        String studentCode,
        String username,
        String email,
        String fullName,
        String phoneNumber,
        Genders gender,
        LocalDate dateOfBirth,
        String educationLevel,
        String profilePhotoUrl,
        String profilePhotoPublicId,
        Double totalPoints,
        Timestamp createdAt,
        Timestamp updatedAt
) {
}
