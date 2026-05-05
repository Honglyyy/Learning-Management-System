package com.ly.lmsbackend.dto;

public record SectionResponseDTO(
        Long sectionId,
        String title,
        String duration,
        Long courseId,
        String courseName
) {
}
