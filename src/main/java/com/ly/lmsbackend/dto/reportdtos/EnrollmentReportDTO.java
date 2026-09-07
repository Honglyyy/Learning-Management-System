package com.ly.lmsbackend.dto.reportdtos;

import lombok.Builder;
import java.util.List;

@Builder
public record EnrollmentReportDTO(
        long totalEnrollments,
        List<MonthlyEnrollmentDTO> monthlyBreakdown,
        List<CourseEnrollmentDTO> courseBreakdown
) {
}
