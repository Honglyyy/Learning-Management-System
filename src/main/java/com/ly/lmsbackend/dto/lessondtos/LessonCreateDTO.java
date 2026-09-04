package com.ly.lmsbackend.dto.lessondtos;

public record LessonCreateDTO(
        String title,
        String videoUrl,
        String videoPublicId,
        Long sectionId
) {
}
