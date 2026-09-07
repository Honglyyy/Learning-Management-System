package com.ly.lmsbackend.dto.reportdtos;

import lombok.Builder;

@Builder
public record QuizSummaryReportDTO(
        Long quizId,
        String quizTitle,
        String courseTitle,
        long totalAttempts,
        long uniqueStudents,
        Double averageScore,
        Double passRate,
        Double highestScore,
        Double lowestScore
) {
}
