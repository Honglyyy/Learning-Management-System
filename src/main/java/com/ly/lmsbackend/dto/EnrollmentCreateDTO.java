package com.ly.lmsbackend.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentCreateDTO(
        @NotNull Long courseId
) {
}
