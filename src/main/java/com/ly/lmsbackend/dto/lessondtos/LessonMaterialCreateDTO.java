package com.ly.lmsbackend.dto.lessondtos;

import jakarta.validation.constraints.NotBlank;

public record LessonMaterialCreateDTO(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "File URL is required")
        String fileUrl,
        String filePublicId,
        String fileType,
        Long fileSize
) {
}
