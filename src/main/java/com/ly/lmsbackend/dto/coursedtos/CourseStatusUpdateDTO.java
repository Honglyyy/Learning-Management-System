package com.ly.lmsbackend.dto.coursedtos;

import com.ly.lmsbackend.model.CourseStatus;
import jakarta.validation.constraints.NotNull;

public record CourseStatusUpdateDTO(
        @NotNull(message = "Course status is required")
        CourseStatus status
) {
}
