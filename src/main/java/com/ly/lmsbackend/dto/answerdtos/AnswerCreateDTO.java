package com.ly.lmsbackend.dto.answerdtos;

public record AnswerCreateDTO(
        String answerText,
        Boolean isCorrect,
        Long questionId
) {
}
