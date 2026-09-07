package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.reportdtos.EnrollmentReportDTO;
import com.ly.lmsbackend.dto.reportdtos.ProgressReportDTO;
import com.ly.lmsbackend.dto.reportdtos.QuizReportDTO;
import com.ly.lmsbackend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/enrollments")
    public ResponseEntity<EnrollmentReportDTO> getEnrollmentReport(Authentication authentication) {
        return ResponseEntity.ok(reportService.getEnrollmentReport(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/progress")
    public ResponseEntity<ProgressReportDTO> getProgressReport(
            Authentication authentication,
            @RequestParam(required = false) Long courseId
    ) {
        return ResponseEntity.ok(reportService.getProgressReport(authentication.getName(), courseId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/quizzes")
    public ResponseEntity<QuizReportDTO> getQuizReport(
            Authentication authentication,
            @RequestParam(required = false) Long quizId
    ) {
        return ResponseEntity.ok(reportService.getQuizReport(authentication.getName(), quizId));
    }
}
