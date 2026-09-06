package com.ly.lmsbackend.dto.progressdtos;

import com.ly.lmsbackend.model.EnrollmentStatus;
import lombok.Builder;

@Builder
public record CourseProgressDTO(
        Long courseId,
        String courseTitle,
        Integer totalLessons,
        Integer completedLessons,
        Integer totalQuizzes,
        Integer completedQuizzes,
        Integer totalAssignments,
        Integer completedAssignments,
        Double progressPercentage,
        Double earnedPoints,
        Double totalPoints,
        EnrollmentStatus status,
        Boolean isCompleted
) {
}
