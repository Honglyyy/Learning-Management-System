package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.enrollmentdtos.EnrollmentResponseDTO;
import com.ly.lmsbackend.model.Enrollments;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    public EnrollmentResponseDTO toDTO(Enrollments enrollment) {
        return new EnrollmentResponseDTO(
                enrollment.getEnrollmentId(),
                enrollment.getUser().getId(),
                enrollment.getUser().getUsername(),
                enrollment.getUser().getEmail(),
                enrollment.getCourse().getCourseId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getInstructor() != null ? enrollment.getCourse().getInstructor().getUsername() : null,
                enrollment.getStatus(),
                enrollment.getEarnedPoints() != null ? enrollment.getEarnedPoints() : 0.0,
                enrollment.getTotalPoints() != null ? enrollment.getTotalPoints() : 0.0,
                enrollment.getEnrolledAt(),
                enrollment.getUpdatedAt()
        );
    }
}
