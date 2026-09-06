package com.ly.lmsbackend.dto.lessondtos;

public record LessonMaterialCreateDTO(
        String title,
        String fileUrl,
        String filePublicId,
        String fileType,
        Long fileSize
) {
}
