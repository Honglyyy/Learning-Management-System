package com.ly.lmsbackend.dto;

public record QuestionCreateDTO(
        String questionText,
        Long quizId
) {
}
