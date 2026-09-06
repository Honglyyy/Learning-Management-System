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
        Boolean isFavorite
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
                CourseLevel.ALL_LEVELS, CourseStatus.PUBLISHED, null, null, 0L, false);
    }
}

