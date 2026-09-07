package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.lessondtos.LessonMaterialCreateDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonMaterialDTO;
import com.ly.lmsbackend.service.LessonMaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LessonMaterialController {

    private final LessonMaterialService lessonMaterialService;

    public LessonMaterialController(LessonMaterialService lessonMaterialService) {
        this.lessonMaterialService = lessonMaterialService;
    }

    @GetMapping("/api/lessons/{lessonId}/materials")
    public ResponseEntity<List<LessonMaterialDTO>> getMaterials(
            @PathVariable Long lessonId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(lessonMaterialService.getMaterials(lessonId, userEmail));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/lessons/{lessonId}/materials")
    public ResponseEntity<LessonMaterialDTO> attachMaterial(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonMaterialCreateDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                lessonMaterialService.attachMaterial(lessonId, dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/materials/{materialId}")
    public ResponseEntity<String> deleteMaterial(
            @PathVariable Long materialId,
            Authentication authentication
    ) {
        lessonMaterialService.deleteMaterial(materialId, authentication.getName());
        return ResponseEntity.ok("Material id " + materialId + " has been deleted successfully");
    }
}
