package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.CourseReviewCreateDTO;
import com.ly.lmsbackend.dto.CourseReviewResponseDTO;
import com.ly.lmsbackend.service.CourseReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseReviewController {

    private final CourseReviewService courseReviewService;

    public CourseReviewController(CourseReviewService courseReviewService) {
        this.courseReviewService = courseReviewService;
    }

    @PostMapping("/api/reviews")
    ResponseEntity<CourseReviewResponseDTO> addReview(@RequestBody CourseReviewCreateDTO dto){
        return ResponseEntity.ok(courseReviewService.addReview(dto));
    }
}
