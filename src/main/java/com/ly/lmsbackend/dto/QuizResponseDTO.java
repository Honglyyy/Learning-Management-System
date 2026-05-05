package com.ly.lmsbackend.dto;

public record QuizResponseDTO(
        Long quizId,
        String title,
        double totalPoints,
        Long lessonId,
        String lessonName
) {
}
