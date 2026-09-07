package com.ly.lmsbackend.dto.reportdtos;

import lombok.Builder;

@Builder
public record CourseProgressReportDTO(
        Long courseId,
        String courseTitle,
        long enrolledCount,
        long completedCount,
        Double completionRate,
        Double averageQuizScore,
        int totalAssignments,
        int totalSubmissions,
        int gradedSubmissions
) {
}
