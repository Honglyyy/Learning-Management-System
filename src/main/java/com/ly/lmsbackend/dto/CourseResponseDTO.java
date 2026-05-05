package com.ly.lmsbackend.dto;

import java.util.List;

public record CourseResponseDTO(
        Long courseId,
        String title,
        String description,
        java.math.BigDecimal price,
        String overallDuration,
        String coverDir,
        String instructor,
        List<String> categories,
        Double rating,
        Long enrollments
) {
}
