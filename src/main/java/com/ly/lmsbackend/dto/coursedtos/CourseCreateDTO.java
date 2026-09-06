package com.ly.lmsbackend.dto.coursedtos;

import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;

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
        List<Long> categoryId,
        CourseLevel level,
        CourseStatus status,
        String learningOutcomes,
        String requirements
) {
    public CourseCreateDTO(
            String title,
            String description,
            BigDecimal price,
            String overallDuration,
            String coverUrl,
            String coverPublicId,
            Long instructor,
            List<Long> categoryId
    ) {
        this(title, description, price, overallDuration, coverUrl, coverPublicId, instructor, categoryId,
                CourseLevel.ALL_LEVELS, CourseStatus.PUBLISHED, null, null);
    }
}

