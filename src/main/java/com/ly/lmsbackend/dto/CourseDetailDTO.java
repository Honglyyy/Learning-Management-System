package com.ly.lmsbackend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseDetailDTO(
        Long courseId,
        String title,
        String description,
        BigDecimal price,
        String overallDuration,
        String coverDir,
        String instructor,
        Long sectionCount,
        Double rating,
        List<String> categories,
        List<SectionDetailDTO> sections,
        List<CourseReviewResponseDTO> reviews
) {
}
