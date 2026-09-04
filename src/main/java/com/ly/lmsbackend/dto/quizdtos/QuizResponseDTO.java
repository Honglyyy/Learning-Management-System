package com.ly.lmsbackend.dto.quizdtos;

public record QuizResponseDTO(
        Long quizId,
        String title,
        double totalPoints,
        Long lessonId,
        String lessonName
) {
}
