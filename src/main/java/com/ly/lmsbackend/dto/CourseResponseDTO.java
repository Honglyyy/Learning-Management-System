package com.ly.lmsbackend.dto;

import java.util.List;

public record CourseResponseDTO(
        Long courseId,
        String title,
        String description,
        java.math.BigDecimal price,
        String overallDuration,
        String coverUrl,
        String coverPublicId,
        Long instructorId,
        String instructor,
        List<Long> categoryIds,
        List<String> categories,
        Double rating
) {
}
