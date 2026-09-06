package com.ly.lmsbackend.dto.lessondtos;

public record LessonDetailDTO(
        Long lessonId,
        String title,
        String videoUrl,
        String videoPublicId,
        String description,
        String textContent,
        Integer orderIndex,
        String duration,
        Boolean isFree
) {
    public LessonDetailDTO(Long lessonId, String title, String videoUrl, String videoPublicId) {
        this(lessonId, title, videoUrl, videoPublicId, null, null, 0, null, false);
    }

    public LessonDetailDTO(Long lessonId, String title, String videoUrl, String videoPublicId, String description, String textContent, Integer orderIndex, String duration) {
        this(lessonId, title, videoUrl, videoPublicId, description, textContent, orderIndex, duration, false);
    }
}
