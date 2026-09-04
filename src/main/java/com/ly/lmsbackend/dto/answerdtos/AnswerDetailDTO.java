package com.ly.lmsbackend.dto.answerdtos;

public record AnswerDetailDTO(
        Long answerId,
        String answerText,
        Boolean isCorrect
) {
}
