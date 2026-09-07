package com.ly.lmsbackend.dto.assignmentdtos;

import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;

public record AssignmentUpdateDTO(
        Long sectionId,
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
