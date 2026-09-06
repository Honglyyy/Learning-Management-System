package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.activitydtos.ActivityLogResponseDTO;
import com.ly.lmsbackend.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/activities")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ActivityLogResponseDTO>> getMyActivities(Authentication authentication) {
        return ResponseEntity.ok(activityLogService.getMyActivities(authentication.getName()));
    }
}
