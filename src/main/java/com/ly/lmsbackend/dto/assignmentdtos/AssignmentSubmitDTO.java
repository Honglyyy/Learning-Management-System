package com.ly.lmsbackend.dto.assignmentdtos;

public record AssignmentSubmitDTO(
        String textSubmission,
        String fileUrl,
        String filePublicId
) {
    public AssignmentSubmitDTO(String textSubmission, String fileUrl) {
        this(textSubmission, fileUrl, null);
    }
}
