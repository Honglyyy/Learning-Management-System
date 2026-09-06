package com.ly.lmsbackend.dto.assignmentdtos;

import com.ly.lmsbackend.model.AssignmentStatus;

import java.sql.Timestamp;

public record AssignmentSubmissionResponseDTO(
        Long submissionId,
        Long assignmentId,
        Long studentId,
        String studentName,
        String studentEmail,
        String textSubmission,
        String fileUrl,
        String filePublicId,
        Timestamp submittedAt,
        AssignmentStatus status,
        Double score,
        String grade,
        String feedback,
        Long gradedById,
        String gradedByName,
        Timestamp gradedAt
) {
    public AssignmentSubmissionResponseDTO(
            Long submissionId,
            Long assignmentId,
            Long studentId,
            String studentName,
            String studentEmail,
            String textSubmission,
            String fileUrl,
            Timestamp submittedAt,
            AssignmentStatus status,
            Double score,
            String grade,
            String feedback,
            Timestamp gradedAt
    ) {
        this(submissionId, assignmentId, studentId, studentName, studentEmail, textSubmission, fileUrl, null, submittedAt, status, score, grade, feedback, null, null, gradedAt);
    }
}
