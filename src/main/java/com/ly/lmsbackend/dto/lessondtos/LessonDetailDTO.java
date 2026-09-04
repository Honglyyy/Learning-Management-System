package com.ly.lmsbackend.dto.lessondtos;

public record LessonDetailDTO(
        Long lessonId,
        String title,
        String videoUrl,
        String videoPublicId
) {
}
