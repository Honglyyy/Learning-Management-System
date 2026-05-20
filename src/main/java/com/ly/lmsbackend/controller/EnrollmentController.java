package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.EnrollmentAdminCreateDTO;
import com.ly.lmsbackend.dto.EnrollmentCreateDTO;
import com.ly.lmsbackend.dto.EnrollmentResponseDTO;
import com.ly.lmsbackend.dto.EnrollmentStatusUpdateDTO;
import com.ly.lmsbackend.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/api/enrollments")
    public ResponseEntity<EnrollmentResponseDTO> enrollCurrentUser(
            @Valid @RequestBody EnrollmentCreateDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                enrollmentService.enrollCurrentUser(dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/enrollments/manage")
    public ResponseEntity<EnrollmentResponseDTO> enrollUser(
            @Valid @RequestBody EnrollmentAdminCreateDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                enrollmentService.enrollUser(dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/enrollments")
    public ResponseEntity<List<EnrollmentResponseDTO>> getAllEnrollments(Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments(authentication.getName()));
    }

    @GetMapping("/api/enrollments/me")
    public ResponseEntity<List<EnrollmentResponseDTO>> getMyEnrollments(Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/courses/{courseId}/enrollments")
    public ResponseEntity<List<EnrollmentResponseDTO>> getCourseEnrollments(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(enrollmentService.getCourseEnrollments(courseId, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PatchMapping("/api/enrollments/{id}/status")
    public ResponseEntity<EnrollmentResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentStatusUpdateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(enrollmentService.updateStatus(id, dto.status(), authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/enrollments/{id}")
    public ResponseEntity<Void> deleteEnrollment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        enrollmentService.deleteEnrollment(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/enrollments/me/courses/{courseId}")
    public ResponseEntity<Void> cancelCurrentUserEnrollment(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        enrollmentService.cancelCurrentUserEnrollment(courseId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
