package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentResponseDTO;
import com.ly.lmsbackend.model.Enrollments;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    public EnrollmentResponseDTO toDTO(Enrollments enrollment) {
        boolean isExpired = enrollment.isExpired();
        Long daysRemaining = null;
        if (enrollment.getExpirationDate() != null) {
            long diffMillis = enrollment.getExpirationDate().getTime() - System.currentTimeMillis();
            daysRemaining = Math.max(0L, diffMillis / (24L * 60L * 60L * 1000L));
        }

        com.ly.lmsbackend.model.EnrollmentStatus effectiveStatus = enrollment.getStatus();
        if (isExpired && effectiveStatus == com.ly.lmsbackend.model.EnrollmentStatus.ACTIVE) {
            effectiveStatus = com.ly.lmsbackend.model.EnrollmentStatus.EXPIRED;
        }

        return new EnrollmentResponseDTO(
                enrollment.getEnrollmentId(),
                enrollment.getUser().getId(),
                enrollment.getUser().getUsername(),
                enrollment.getUser().getEmail(),
                enrollment.getCourse().getCourseId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getInstructor() != null ? enrollment.getCourse().getInstructor().getUsername() : null,
                effectiveStatus,
                enrollment.getEarnedPoints() != null ? enrollment.getEarnedPoints() : 0.0,
                enrollment.getTotalPoints() != null ? enrollment.getTotalPoints() : 0.0,
                enrollment.getEnrolledAt(),
                enrollment.getUpdatedAt(),
                enrollment.getExpirationDate(),
                isExpired,
                daysRemaining
        );
    }
}
