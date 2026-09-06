package com.ly.lmsbackend.dto.assignmentdtos;

import java.sql.Timestamp;

public record AssignmentResponseDTO(
        Long assignmentId,
        Long courseId,
        String courseTitle,
        Long sectionId,
        String sectionTitle,
        String title,
        String description,
        String instructions,
        Timestamp startDate,
        Timestamp dueDate,
        Double maxScore,
        String supportingFileUrl,
        String supportingFilePublicId,
        Boolean allowResubmission,
        Long instructorId,
        String instructorName,
        Timestamp createdAt,
        Timestamp updatedAt
) {
    public AssignmentResponseDTO(
            Long assignmentId,
            Long courseId,
            String courseTitle,
            String title,
            String description,
            String instructions,
            Timestamp startDate,
            Timestamp dueDate,
            Double maxScore,
            String supportingFileUrl,
            Boolean allowResubmission,
            Timestamp createdAt
    ) {
        this(assignmentId, courseId, courseTitle, null, null, title, description, instructions, startDate, dueDate, maxScore, supportingFileUrl, null, allowResubmission, null, null, createdAt, null);
    }
}
