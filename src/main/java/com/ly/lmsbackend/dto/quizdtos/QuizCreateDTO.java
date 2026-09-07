package com.ly.lmsbackend.dto.quizdtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record QuizCreateDTO(
        @NotBlank(message = "Quiz title is required")
        String title,

        @PositiveOrZero(message = "Total points must be positive or zero")
        double totalPoints,

        @NotNull(message = "Lesson ID is required")
        Long lessonId
) {
}
