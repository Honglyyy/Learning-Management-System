package com.ly.lmsbackend.dto.enrollmentdtos;

import jakarta.validation.constraints.NotNull;

public record EnrollmentCreateDTO(
        @NotNull Long courseId
) {
}
