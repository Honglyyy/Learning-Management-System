package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.assignmentdtos.*;
import com.ly.lmsbackend.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/assignments")
    public ResponseEntity<AssignmentResponseDTO> createAssignment(
            @Valid @RequestBody AssignmentCreateDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                assignmentService.createAssignment(dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT', 'USER')")
    @GetMapping("/api/courses/{courseId}/assignments")
    public ResponseEntity<List<AssignmentResponseDTO>> getCourseAssignments(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(assignmentService.getCourseAssignments(courseId, email));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT', 'USER')")
    @GetMapping("/api/assignments/{id}")
    public ResponseEntity<AssignmentResponseDTO> getAssignmentById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(assignmentService.getAssignmentById(id, email));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/assignments/{id}")
    public ResponseEntity<AssignmentResponseDTO> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, dto, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/assignments/{id}")
    public ResponseEntity<String> deleteAssignment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        assignmentService.deleteAssignment(id, authentication.getName());
        return ResponseEntity.ok("Assignment id " + id + " has been deleted successfully");
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @PostMapping("/api/assignments/{id}/submit")
    public ResponseEntity<AssignmentSubmissionResponseDTO> submitAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentSubmitDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                assignmentService.submitAssignment(id, dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER', 'ADMIN')")
    @GetMapping("/api/assignments/{id}/submission/me")
    public ResponseEntity<AssignmentSubmissionResponseDTO> getMySubmission(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(assignmentService.getMySubmission(id, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/assignments/{id}/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponseDTO>> getAssignmentSubmissions(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentSubmissions(id, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/assignments/submissions/{id}/grade")
    public ResponseEntity<AssignmentSubmissionResponseDTO> gradeSubmission(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentGradeDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(assignmentService.gradeSubmission(id, dto, authentication.getName()));
    }
}
