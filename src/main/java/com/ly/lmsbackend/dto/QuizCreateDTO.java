package com.ly.lmsbackend.dto;

public record QuizCreateDTO(
        String title,
        double totalPoints,
        Long lessonId
) {
}
