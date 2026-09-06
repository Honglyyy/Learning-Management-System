package com.ly.lmsbackend.dto.lessondtos;

public record LessonCreateDTO(
        String title,
        String videoUrl,
        String videoPublicId,
        Long sectionId,
        String description,
        String textContent,
        Integer orderIndex,
        String duration,
        Boolean isFree
) {
    public LessonCreateDTO(String title, String videoUrl, String videoPublicId, Long sectionId) {
        this(title, videoUrl, videoPublicId, sectionId, null, null, 0, null, false);
    }

    public LessonCreateDTO(String title, String videoUrl, String videoPublicId, Long sectionId, String description, String textContent, Integer orderIndex, String duration) {
        this(title, videoUrl, videoPublicId, sectionId, description, textContent, orderIndex, duration, false);
    }
}
