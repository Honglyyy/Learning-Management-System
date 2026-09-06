package com.ly.lmsbackend.dto.assignmentdtos;

import java.sql.Timestamp;

public record AssignmentCreateDTO(
        Long courseId,
        Long sectionId,
        String title,
        String description,
        String instructions,
        Timestamp startDate,
        Timestamp dueDate,
        Double maxScore,
        String supportingFileUrl,
        String supportingFilePublicId,
        Boolean allowResubmission
) {
}
