package com.ly.lmsbackend.dto.answerdtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerCreateDTO(
        @NotBlank(message = "Answer text is required")
        String answerText,

        Boolean isCorrect,

        @NotNull(message = "Question ID is required")
        Long questionId
) {
}
