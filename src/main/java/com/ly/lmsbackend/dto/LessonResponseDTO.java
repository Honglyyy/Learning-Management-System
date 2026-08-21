package com.ly.lmsbackend.dto;

public record LessonResponseDTO(
        Long lessonId,
        String title,
        String videoUrl,
        String videoPublicId,
        Long sectionId,
        String sectionName
) {
}
