package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.coursereviewdtos.CourseReviewCreateDTO;
import com.ly.lmsbackend.dto.coursereviewdtos.CourseReviewResponseDTO;
import com.ly.lmsbackend.service.CourseReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CourseReviewController {

    private final CourseReviewService courseReviewService;

    public CourseReviewController(CourseReviewService courseReviewService) {
        this.courseReviewService = courseReviewService;
    }

//    @PostMapping("/api/reviews")
//    ResponseEntity<CourseReviewResponseDTO> addReview(@Valid @RequestBody CourseReviewCreateDTO dto, Authentication authentication) {
//        return ResponseEntity.ok(courseReviewService.addReview(dto,authentication.getName()));
//    }

    @GetMapping("/api/reviews")
    ResponseEntity<List<CourseReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok(courseReviewService.getAllReviews());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/courses/{courseId}/review")
    public ResponseEntity<CourseReviewResponseDTO> addReview(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseReviewCreateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                courseReviewService.addReviewByCourse(courseId, dto, authentication.getName())
        );
    }
    @GetMapping("/api/courses/{courseId}/reviews")
    public ResponseEntity<List<CourseReviewResponseDTO>> getByCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(courseReviewService.getByCourse(courseId));
    }
}
