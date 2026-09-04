package com.ly.lmsbackend.dto.enrollmentdtos;

import com.ly.lmsbackend.model.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record EnrollmentStatusUpdateDTO(
        @NotNull EnrollmentStatus status
) {
}
