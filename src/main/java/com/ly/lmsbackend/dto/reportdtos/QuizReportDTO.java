package com.ly.lmsbackend.dto.reportdtos;

import lombok.Builder;
import java.util.List;

@Builder
public record QuizReportDTO(
        int totalQuizzes,
        long totalAttempts,
        Double overallPassRate,
        Double overallAverageScore,
        List<QuizSummaryReportDTO> quizSummaries,
        List<HardestQuestionDTO> hardestQuestions
) {
}
