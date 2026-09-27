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
        Double earnedPoints,
        Double totalPoints,
        Timestamp enrolledAt,
        Timestamp updatedAt,
        Timestamp expirationDate,
        Boolean isExpired,
        Long daysRemaining
) {
    public EnrollmentResponseDTO(
            Long enrollmentId,
            Long userId,
            String username,
            String userEmail,
            Long courseId,
            String courseTitle,
            String instructor,
            EnrollmentStatus status,
            Double earnedPoints,
            Double totalPoints,
            Timestamp enrolledAt,
            Timestamp updatedAt
    ) {
        this(enrollmentId, userId, username, userEmail, courseId, courseTitle, instructor, status,
                earnedPoints, totalPoints, enrolledAt, updatedAt, null, false, null);
    }
}
