package com.ly.lmsbackend.dto;

public record LessonResponseDTO(
        Long lessonId,
        String title,
        String videoDir,
        String sectionName
) {
}
