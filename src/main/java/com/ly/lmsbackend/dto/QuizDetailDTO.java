package com.ly.lmsbackend.dto;

import java.util.List;

public record QuizDetailDTO(
        Long quizId,
        String title,
        double totalPoints,
        List<QuestionDetailDTO> question
) {
}
