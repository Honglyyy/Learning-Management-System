package com.ly.lmsbackend.dto.questiondtos;

public record QuestionResponseDTO(
        Long questionId,
        String questionText,
        Long point,
        Long quizId
) {
}
