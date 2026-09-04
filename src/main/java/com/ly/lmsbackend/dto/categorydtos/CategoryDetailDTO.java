package com.ly.lmsbackend.dto.categorydtos;

import com.ly.lmsbackend.dto.coursedtos.CourseDTO;

import java.util.List;

public record CategoryDetailDTO(
        Long categoryId,
        String category,
        List<CourseDTO> courses
) {
}
