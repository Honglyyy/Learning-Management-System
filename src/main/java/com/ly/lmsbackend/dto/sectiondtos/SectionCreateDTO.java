package com.ly.lmsbackend.dto.sectiondtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionCreateDTO(
        @NotBlank(message = "Section title is required")
        String title,

        String duration,

        @NotNull(message = "Course ID is required")
        Long courseId
) {
}
