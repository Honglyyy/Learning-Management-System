package com.ly.lmsbackend.dto.dashboarddtos;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AdminDashboardDTO(
        long totalStudents,
        long totalInstructors,
        long totalCourses,
        long totalEnrollments,
        long completedEnrollments,
        Double completionRate,
        BigDecimal grossRevenue,
        long totalQuizzes,
        long totalAssignments
) {
}
