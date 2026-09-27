package com.ly.lmsbackend.dto.coursedtos;

import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;

import java.math.BigDecimal;
import java.util.List;

public record CourseResponseDTO(
        Long courseId,
        String title,
        String description,
        BigDecimal price,
        String overallDuration,
        String coverUrl,
        String coverPublicId,
        Long instructorId,
        String instructor,
        List<Long> categoryIds,
        List<String> categories,
        Double rating,
        CourseLevel level,
        CourseStatus status,
        Long lessonCount,
        Long enrollmentCount,
        Boolean isFavorite,
        Integer accessDurationDays
) {
    public CourseResponseDTO(
            Long courseId,
            String title,
            String description,
            BigDecimal price,
            String overallDuration,
            String coverUrl,
            String coverPublicId,
            Long instructorId,
            String instructor,
            List<Long> categoryIds,
            List<String> categories,
            Double rating
    ) {
        this(courseId, title, description, price, overallDuration, coverUrl, coverPublicId,
                instructorId, instructor, categoryIds, categories, rating,
                CourseLevel.ALL_LEVELS, CourseStatus.PUBLISHED, 0L, 0L, false, 180);
    }

    public CourseResponseDTO(
            Long courseId,
            String title,
            String description,
            BigDecimal price,
            String overallDuration,
            String coverUrl,
            String coverPublicId,
            Long instructorId,
            String instructor,
            List<Long> categoryIds,
            List<String> categories,
            Double rating,
            CourseLevel level,
            CourseStatus status,
            Long lessonCount,
            Long enrollmentCount,
            Boolean isFavorite
    ) {
        this(courseId, title, description, price, overallDuration, coverUrl, coverPublicId,
                instructorId, instructor, categoryIds, categories, rating,
                level, status, lessonCount, enrollmentCount, isFavorite, 180);
    }
}

