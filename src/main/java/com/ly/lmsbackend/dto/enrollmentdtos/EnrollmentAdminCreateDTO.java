package com.ly.lmsbackend.dto.enrollmentdtos;

import com.ly.lmsbackend.model.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record EnrollmentAdminCreateDTO(
        @NotNull Long userId,
        @NotNull Long courseId,
        EnrollmentStatus status
) {
}
