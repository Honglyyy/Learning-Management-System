package com.ly.lmsbackend.dto;

import java.util.List;

public record CategoryDetailDTO(
        Long categoryId,
        String category,
        List<CourseDTO> courses
) {
}
