package com.ly.lmsbackend.dto.coursedtos;

import java.util.List;

public record MyCoursesSummaryDTO(
        List<CourseResponseDTO> inProgressCourses,
        List<CourseResponseDTO> completedCourses,
        List<CourseResponseDTO> savedCourses,
        List<CourseResponseDTO> expiredCourses
) {
    public MyCoursesSummaryDTO(
            List<CourseResponseDTO> inProgressCourses,
            List<CourseResponseDTO> completedCourses,
            List<CourseResponseDTO> savedCourses
    ) {
        this(inProgressCourses, completedCourses, savedCourses, List.of());
    }
}
