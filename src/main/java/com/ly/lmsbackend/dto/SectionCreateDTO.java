package com.ly.lmsbackend.dto;

public record SectionCreateDTO(
        String title,
        String duration,
        Long courseId
) {
}
