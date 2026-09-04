package com.ly.lmsbackend.dto.coursedtos;

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
