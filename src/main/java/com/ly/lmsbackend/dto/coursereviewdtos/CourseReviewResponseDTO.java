package com.ly.lmsbackend.dto.coursereviewdtos;

public record CourseReviewResponseDTO(
        Long reviewId,
        String reviewText,
        Integer rating,
        String username,
        String courseTitle
) {}