package com.ly.lmsbackend.dto.questiondtos;

public record QuestionCreateDTO(
        String questionText,
        Long point,
        Long quizId
) {
}
