package com.ly.lmsbackend.dto.enrollmentdtos;

import com.ly.lmsbackend.model.EnrollmentStatus;

import java.sql.Timestamp;

public record EnrollmentResponseDTO(
        Long enrollmentId,
        Long userId,
        String username,
        String userEmail,
        Long courseId,
        String courseTitle,
        String instructor,
        EnrollmentStatus status,
        Timestamp enrolledAt,
        Timestamp updatedAt
) {
}
