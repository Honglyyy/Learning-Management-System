package com.ly.lmsbackend.dto.lessondtos;

public record LessonMaterialDTO(
        Long materialId,
        Long lessonId,
        String title,
        String fileUrl,
        String fileType,
        Long fileSize
) {
}
