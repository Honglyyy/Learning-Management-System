package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.dashboarddtos.AdminDashboardDTO;
import com.ly.lmsbackend.dto.dashboarddtos.InstructorDashboardDTO;
import com.ly.lmsbackend.dto.dashboarddtos.StudentDashboardDTO;
import com.ly.lmsbackend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasAnyRole('STUDENT', 'USER')")
    @GetMapping("/student")
    public ResponseEntity<StudentDashboardDTO> getStudentDashboard(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/instructor")
    public ResponseEntity<InstructorDashboardDTO> getInstructorDashboard(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getInstructorDashboard(authentication.getName()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
}
