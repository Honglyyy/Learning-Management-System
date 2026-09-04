package com.ly.lmsbackend.dto.sectiondtos;

public record SectionCreateDTO(
        String title,
        String duration,
        Long courseId
) {
}
