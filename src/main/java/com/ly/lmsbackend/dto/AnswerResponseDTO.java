package com.ly.lmsbackend.dto;

public record AnswerResponseDTO(
        Long answerId,
        String answerText,
        Boolean isCorrect,
        Long questionId
) {
}
