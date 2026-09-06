package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.studentdtos.StudentProfileResponseDTO;
import com.ly.lmsbackend.dto.studentdtos.StudentProfileUpdateDTO;
import com.ly.lmsbackend.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentProfileController {
    private final StudentService studentService;

    @PreAuthorize("hasAnyRole('STUDENT', 'USER')")
    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponseDTO> getProfile(Authentication authentication) {
        return ResponseEntity.ok(studentService.getProfile(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER')")
    @PutMapping("/profile")
    public ResponseEntity<StudentProfileResponseDTO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody StudentProfileUpdateDTO dto
    ) {
        return ResponseEntity.ok(studentService.updateProfile(authentication.getName(), dto));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'USER')")
    @PostMapping(value = "/profile/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentProfileResponseDTO> uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(studentService.uploadProfilePhoto(authentication.getName(), file));
    }
}
