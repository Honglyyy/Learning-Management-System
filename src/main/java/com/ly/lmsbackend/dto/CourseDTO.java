package com.ly.lmsbackend.dto;

public record CourseDTO(
        Long courseId,
        String title,
        String description,
        java.math.BigDecimal price,
        String overallDuration,
        String coverUrl,
        String coverPublicId,
        String instructor
) {
}
