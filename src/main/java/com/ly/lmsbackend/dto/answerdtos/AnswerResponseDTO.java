package com.ly.lmsbackend.dto.answerdtos;

public record AnswerResponseDTO(
        Long answerId,
        String answerText,
        Boolean isCorrect,
        Long questionId
) {
}
