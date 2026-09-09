package com.ly.lmsbackend.dto.coursedtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
        String instructorUsername,
        List<Long> categoryId,
        CourseLevel level,
        CourseStatus status,
        String learningOutcomes,
        String requirements
) {
    @JsonCreator
    public static CourseCreateDTO fromJson(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("overallDuration") String overallDuration,
            @JsonProperty("coverUrl") String coverUrl,
            @JsonProperty("coverPublicId") String coverPublicId,
            @JsonProperty("instructor") Object instructorObj,
            @JsonProperty("instructorId") Long instructorId,
            @JsonProperty("instructorUsername") String instructorUsername,
            @JsonProperty("categoryId") List<Long> categoryId,
            @JsonProperty("categoryIds") List<Long> categoryIds,
            @JsonProperty("level") CourseLevel level,
            @JsonProperty("status") CourseStatus status,
            @JsonProperty("learningOutcomes") String learningOutcomes,
            @JsonProperty("requirements") String requirements
    ) {
        Long resolvedInstructor = instructorId;
        String resolvedInstructorUsername = instructorUsername;

        if (instructorObj != null) {
            if (instructorObj instanceof Number num) {
                if (resolvedInstructor == null) {
                    resolvedInstructor = num.longValue();
                }
            } else if (instructorObj instanceof String str) {
                String text = str.trim();
                try {
                    long parsed = Long.parseLong(text);
                    if (resolvedInstructor == null) {
                        resolvedInstructor = parsed;
                    }
                } catch (NumberFormatException e) {
                    // String username like "adorablie_"
                    if (resolvedInstructorUsername == null || resolvedInstructorUsername.isBlank()) {
                        resolvedInstructorUsername = text;
                    }
                }
            }
        }

        List<Long> resolvedCategories = (categoryId != null && !categoryId.isEmpty())
                ? categoryId
                : categoryIds;

        return new CourseCreateDTO(
                title,
                description,
                price,
                overallDuration,
                coverUrl,
                coverPublicId,
                resolvedInstructor,
                resolvedInstructorUsername,
                resolvedCategories,
                level != null ? level : CourseLevel.ALL_LEVELS,
                status != null ? status : CourseStatus.PUBLISHED,
                learningOutcomes,
                requirements
        );
    }

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
        this(title, description, price, overallDuration, coverUrl, coverPublicId, instructor, null, categoryId,
                CourseLevel.ALL_LEVELS, CourseStatus.PUBLISHED, null, null);
    }

    public CourseCreateDTO(
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
        this(title, description, price, overallDuration, coverUrl, coverPublicId, instructor, null, categoryId,
                level, status, learningOutcomes, requirements);
    }
}

