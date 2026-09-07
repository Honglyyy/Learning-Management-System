package com.ly.lmsbackend.dto.dashboarddtos;

import com.ly.lmsbackend.dto.progressdtos.ContinueLearningDTO;
import lombok.Builder;

@Builder
public record StudentDashboardDTO(
        long totalEnrolledCourses,
        long inProgressCourses,
        long completedCourses,
        Double averageQuizScore,
        Double totalEarnedPoints,
        ContinueLearningDTO continueLearning
) {
}
