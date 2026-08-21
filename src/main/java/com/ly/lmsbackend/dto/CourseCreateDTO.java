package com.ly.lmsbackend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseCreateDTO(
        String title,
        String description,
        BigDecimal price,
        String overallDuration,
        String coverUrl,
        String coverPublicId,
        Long instructor,
        List<Long> categoryId
) {
}
