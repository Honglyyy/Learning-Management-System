package com.ly.lmsbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;

public record CourseReviewCreateDTO(
        @NotBlank
        String reviewText,

        @NotNull
        @Min(1)
        @Max(5)
        Integer rating
) {}