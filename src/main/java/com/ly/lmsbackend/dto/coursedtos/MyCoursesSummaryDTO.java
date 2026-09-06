package com.ly.lmsbackend.dto.coursedtos;

import java.util.List;

public record MyCoursesSummaryDTO(
        List<CourseResponseDTO> inProgressCourses,
        List<CourseResponseDTO> completedCourses,
        List<CourseResponseDTO> savedCourses
) {
}
