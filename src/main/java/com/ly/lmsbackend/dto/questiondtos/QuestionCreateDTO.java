package com.ly.lmsbackend.dto.questiondtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record QuestionCreateDTO(
        @NotBlank(message = "Question text is required")
        String questionText,

        @NotNull(message = "Point is required")
        @PositiveOrZero(message = "Point must be positive or zero")
        Long point,

        @NotNull(message = "Quiz ID is required")
        Long quizId
) {
}
