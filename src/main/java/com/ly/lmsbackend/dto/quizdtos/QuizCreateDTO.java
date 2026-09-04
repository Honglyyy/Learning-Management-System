package com.ly.lmsbackend.dto.quizdtos;

public record QuizCreateDTO(
        String title,
        double totalPoints,
        Long lessonId
) {
}
