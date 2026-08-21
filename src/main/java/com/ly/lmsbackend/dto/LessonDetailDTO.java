package com.ly.lmsbackend.dto;

public record LessonDetailDTO(
        Long lessonId,
        String title,
        String videoUrl,
        String videoPublicId
) {
}
