package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.progressdtos.ContinueLearningDTO;
import com.ly.lmsbackend.dto.progressdtos.CourseProgressDTO;
import com.ly.lmsbackend.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @GetMapping("/api/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressDTO> getCourseProgress(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(progressService.getCourseProgress(courseId, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @GetMapping("/api/learning/continue")
    public ResponseEntity<ContinueLearningDTO> getContinueLearning(Authentication authentication) {
        return ResponseEntity.ok(progressService.getContinueLearning(authentication.getName()));
    }
}
