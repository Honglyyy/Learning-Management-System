package com.ly.lmsbackend.dto;

public record QuestionResponseDTO(
        Long questionId,
        String questionText,
        Long point,
        Long quizId
) {
}
