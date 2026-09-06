package com.ly.lmsbackend.dto.assignmentdtos;

public record AssignmentGradeDTO(
        Double score,
        String grade,
        String feedback
) {
}
