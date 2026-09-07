package com.ly.lmsbackend.dto.reportdtos;

import lombok.Builder;

@Builder
public record HardestQuestionDTO(
        Long questionId,
        String questionText,
        long totalAttempts,
        long incorrectCount,
        Double accuracyRate
) {
}
