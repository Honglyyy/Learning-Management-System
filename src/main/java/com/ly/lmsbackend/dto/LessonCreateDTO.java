package com.ly.lmsbackend.dto;

public record LessonCreateDTO(
        String title,
        String videoUrl,
        String videoPublicId,
        Long sectionId
) {
}
