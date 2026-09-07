package com.ly.lmsbackend.dto.reportdtos;

import lombok.Builder;
import java.util.List;

@Builder
public record ProgressReportDTO(
        long totalStudentsEnrolled,
        Double overallCompletionRate,
        Double overallAverageQuizScore,
        Double assignmentSubmissionRate,
        List<CourseProgressReportDTO> courseProgressList
) {
}
