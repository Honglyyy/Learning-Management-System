package com.ly.lmsbackend.dto.dashboarddtos;

import lombok.Builder;

@Builder
public record InstructorDashboardDTO(
        long totalCoursesCreated,
        long activeStudentCount,
        long totalEnrollments,
        long pendingSubmissionsToGrade,
        Double averageCourseRating
) {
}
