package com.ly.lmsbackend.dto.coursedtos;

import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record CourseCreateDTO(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be positive or zero")
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

