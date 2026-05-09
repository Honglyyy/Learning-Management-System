package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.CourseReviewCreateDTO;
import com.ly.lmsbackend.dto.CourseReviewResponseDTO;
import com.ly.lmsbackend.service.CourseReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseReviewController {

    private final CourseReviewService courseReviewService;

    public CourseReviewController(CourseReviewService courseReviewService) {
        this.courseReviewService = courseReviewService;
    }

    @PostMapping("/api/reviews")
    ResponseEntity<CourseReviewResponseDTO> addReview(@Valid @RequestBody CourseReviewCreateDTO dto, Authentication authentication) {
        return ResponseEntity.ok(courseReviewService.addReview(dto,authentication.getName()));
    }

    @GetMapping("/api/reviews")
    ResponseEntity<List<CourseReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok(courseReviewService.getAllReviews());
    }
}
