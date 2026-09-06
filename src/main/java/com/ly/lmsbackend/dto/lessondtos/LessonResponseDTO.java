package com.ly.lmsbackend.dto.lessondtos;

public record LessonResponseDTO(
        Long lessonId,
        String title,
        String videoUrl,
        String videoPublicId,
        Long sectionId,
        String sectionName,
        String description,
        String textContent,
        Integer orderIndex,
        String duration,
        Boolean isFree
) {
    public LessonResponseDTO(Long lessonId, String title, String videoUrl, String videoPublicId, Long sectionId, String sectionName) {
        this(lessonId, title, videoUrl, videoPublicId, sectionId, sectionName, null, null, 0, null, false);
    }

    public LessonResponseDTO(Long lessonId, String title, String videoUrl, String videoPublicId, Long sectionId, String sectionName, String description, String textContent, Integer orderIndex, String duration) {
        this(lessonId, title, videoUrl, videoPublicId, sectionId, sectionName, description, textContent, orderIndex, duration, false);
    }
}
