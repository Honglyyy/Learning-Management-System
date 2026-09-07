package com.ly.lmsbackend.dto.assignmentdtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;

public record AssignmentCreateDTO(
        @NotNull(message = "Course ID is required")
        Long courseId,

        Long sectionId,

        @NotBlank(message = "Title is required")
        String title,

        String description,
        String instructions,
        Timestamp startDate,
        Timestamp dueDate,

        @PositiveOrZero(message = "Max score must be positive or zero")
        Double maxScore,
        String supportingFileUrl,
        String supportingFilePublicId,
        Boolean allowResubmission
) {
}
