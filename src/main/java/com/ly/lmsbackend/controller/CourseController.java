package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.coursedtos.*;
import com.ly.lmsbackend.model.CourseLevel;
import com.ly.lmsbackend.model.CourseStatus;
import com.ly.lmsbackend.service.CourseService;
import com.ly.lmsbackend.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class CourseController {

    private final CourseService courseService;
    private final SectionService sectionService;

    public CourseController(CourseService courseService, SectionService sectionService) {
        this.sectionService = sectionService;
        this.courseService = courseService;
    }

    @GetMapping("/api/courses")
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<CourseResponseDTO> courses = courseService.searchAndFilterCourses(
                query, categoryId, level, minRating, maxPrice, status, sort, userEmail
        );
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/api/courses/search")
    public ResponseEntity<List<CourseResponseDTO>> searchCourses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<CourseResponseDTO> courses = courseService.searchAndFilterCourses(
                query, categoryId, level, minRating, maxPrice, CourseStatus.PUBLISHED, sort, userEmail
        );
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/api/courses/featured")
    public ResponseEntity<List<CourseResponseDTO>> getFeaturedCourses(Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(courseService.getFeaturedCourses(userEmail));
    }

    @GetMapping("/api/courses/popular")
    public ResponseEntity<List<CourseResponseDTO>> getPopularCourses(Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(courseService.getPopularCourses(userEmail));
    }

    @GetMapping("/api/courses/my-learning")
    public ResponseEntity<MyCoursesSummaryDTO> getMyCoursesSummary(Authentication authentication) {
        return ResponseEntity.ok(courseService.getMyCoursesSummary(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PatchMapping("/api/courses/{id}/status")
    public ResponseEntity<CourseResponseDTO> updateCourseStatus(
            @PathVariable Long id,
            @RequestBody(required = false) CourseStatusUpdateDTO dto,
            @RequestParam(required = false) CourseStatus status,
            Authentication authentication
    ) {
        CourseStatus targetStatus = (dto != null && dto.status() != null) ? dto.status() : status;
        if (targetStatus == null) {
            throw new IllegalArgumentException("Course status must be provided");
        }
        return ResponseEntity.ok(courseService.updateCourseStatus(id, targetStatus, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/courses")
    public ResponseEntity<CourseResponseDTO> createCourse(@Valid @RequestBody CourseCreateDTO dto) {
        return new ResponseEntity<>(courseService.addCourse(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/courses/instructor/me")
    public ResponseEntity<List<CourseResponseDTO>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getCoursesByInstructor(authentication.getName()));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/api/courses/instructor/me")
    public ResponseEntity<CourseResponseDTO> createMyCourse(
            @Valid @RequestBody CourseCreateDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                courseService.addCourseForInstructor(dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/courses/instructor/me/{id}")
    public void deleteMyCourse(
            @PathVariable Long id,
            Authentication authentication
    ) {
        courseService.deleteMyCourse(id, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/courses/instructor/me/{id}")
    public ResponseEntity<CourseResponseDTO> updateMyCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseCreateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                courseService.updateMyCourse(
                        id,
                        dto,
                        authentication.getName()
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/courses/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return new ResponseEntity<>("Course id " + id + " has now deleted!!", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/courses/{id}")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseCreateDTO dto
    ) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto));
    }

    @GetMapping("/api/courses/{id}")
    public ResponseEntity<CourseDetailDTO> getSectionByCourseId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return new ResponseEntity<>(courseService.getCourseDetail(id, userEmail), HttpStatus.OK);
    }
}
