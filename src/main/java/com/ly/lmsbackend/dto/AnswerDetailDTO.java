package com.ly.lmsbackend.dto;

public record AnswerDetailDTO(
        Long answerId,
        String answerText,
        Boolean isCorrect
) {
}
