package com.ly.lmsbackend.dto.coursedtos;

import com.ly.lmsbackend.dto.coursereviewdtos.CourseReviewResponseDTO;
import com.ly.lmsbackend.dto.sectiondtos.SectionDetailDTO;
import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;

import java.math.BigDecimal;
import java.util.List;

public record CourseDetailDTO(
        Long courseId,
        String title,
        String description,
        BigDecimal price,
        String overallDuration,
        String coverUrl,
        String coverPublicId,
        String instructor,
        Long sectionCount,
        Double rating,
        List<String> categories,
        List<SectionDetailDTO> sections,
        List<CourseReviewResponseDTO> reviews,
        CourseLevel level,
        CourseStatus status,
        String learningOutcomes,
        String requirements,
        Long enrollmentCount,
        Boolean isFavorite,
        Integer accessDurationDays,
        Boolean isEnrolled,
        Boolean isExpired,
        java.sql.Timestamp expirationDate,
        Boolean hasReEnrollmentDiscount,
        BigDecimal discountedPrice
) {
    public CourseDetailDTO(
            Long courseId,
            String title,
            String description,
            BigDecimal price,
            String overallDuration,
            String coverUrl,
            String coverPublicId,
            String instructor,
            Long sectionCount,
            Double rating,
            List<String> categories,
            List<SectionDetailDTO> sections,
            List<CourseReviewResponseDTO> reviews
    ) {
        this(courseId, title, description, price, overallDuration, coverUrl, coverPublicId,
                instructor, sectionCount, rating, categories, sections, reviews,
                CourseLevel.ALL_LEVELS, CourseStatus.PUBLISHED, null, null, 0L, false,
                180, false, false, null, false, null);
    }

    public CourseDetailDTO(
            Long courseId,
            String title,
            String description,
            BigDecimal price,
            String overallDuration,
            String coverUrl,
            String coverPublicId,
            String instructor,
            Long sectionCount,
            Double rating,
            List<String> categories,
            List<SectionDetailDTO> sections,
            List<CourseReviewResponseDTO> reviews,
            CourseLevel level,
            CourseStatus status,
            String learningOutcomes,
            String requirements,
            Long enrollmentCount,
            Boolean isFavorite
    ) {
        this(courseId, title, description, price, overallDuration, coverUrl, coverPublicId,
                instructor, sectionCount, rating, categories, sections, reviews,
                level, status, learningOutcomes, requirements, enrollmentCount, isFavorite,
                180, false, false, null, false, null);
    }
}

