package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.instructordtos.InstructorProfileUpdateDTO;
import com.ly.lmsbackend.dto.instructordtos.InstructorResponseDTO;
import com.ly.lmsbackend.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {
    private final InstructorService instructorService;

    @GetMapping
    public ResponseEntity<List<InstructorResponseDTO>> getAllInstructors() {
        return ResponseEntity.ok(instructorService.getAllInstructors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorResponseDTO> getInstructorById(@PathVariable Long id) {
        return ResponseEntity.ok(instructorService.getInstructorById(id));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/me")
    public ResponseEntity<InstructorResponseDTO> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(instructorService.getMyProfile(authentication.getName()));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/me")
    public ResponseEntity<InstructorResponseDTO> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody InstructorProfileUpdateDTO dto
    ) {
        return ResponseEntity.ok(instructorService.updateMyProfile(authentication.getName(), dto));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InstructorResponseDTO> uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(instructorService.uploadProfilePhoto(authentication.getName(), file));
    }
}
