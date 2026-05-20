package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.EnrollmentResponseDTO;
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
                enrollment.getCourse().getInstructor().getUsername(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getUpdatedAt()
        );
    }
}
