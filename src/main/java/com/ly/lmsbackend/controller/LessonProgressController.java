package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.lessondtos.LessonNavigationDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonProgressDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonStatusDTO;
import com.ly.lmsbackend.service.LessonProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    public LessonProgressController(LessonProgressService lessonProgressService) {
        this.lessonProgressService = lessonProgressService;
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @PostMapping("/api/lessons/{lessonId}/complete")
    public ResponseEntity<LessonProgressDTO> markComplete(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(lessonProgressService.markComplete(lessonId, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @PostMapping("/api/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressDTO> saveProgress(
            @PathVariable Long lessonId,
            @RequestParam(name = "seconds", defaultValue = "0") Double seconds,
            Authentication authentication
    ) {
        return ResponseEntity.ok(lessonProgressService.savePlaybackPosition(lessonId, seconds, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @GetMapping("/api/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressDTO> getProgress(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(lessonProgressService.getProgress(lessonId, authentication.getName()));
    }

    @GetMapping("/api/lessons/{lessonId}/navigation")
    public ResponseEntity<LessonNavigationDTO> getNavigation(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(lessonProgressService.getNavigation(lessonId, userEmail));
    }

    @GetMapping("/api/courses/{courseId}/lessons-status")
    public ResponseEntity<List<LessonStatusDTO>> getLessonsStatus(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(lessonProgressService.getLessonsStatus(courseId, userEmail));
    }
}
