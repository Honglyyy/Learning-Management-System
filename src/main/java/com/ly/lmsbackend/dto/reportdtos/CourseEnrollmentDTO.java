package com.ly.lmsbackend.dto.reportdtos;

public record CourseEnrollmentDTO(
        Long courseId,
        String courseTitle,
        String instructorName,
        long enrollmentCount,
        long activeCount,
        long completedCount
) {
}
