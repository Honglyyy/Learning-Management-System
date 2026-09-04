package com.ly.lmsbackend.dto.sectiondtos;

public record SectionResponseDTO(
        Long sectionId,
        String title,
        String duration,
        Long courseId,
        String courseName
) {
}
