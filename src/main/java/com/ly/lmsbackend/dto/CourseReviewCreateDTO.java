package com.ly.lmsbackend.dto;

public record CourseReviewCreateDTO(
        String reviewText,
        Double rating,
        Long userId,
        Long courseId
) {
}
