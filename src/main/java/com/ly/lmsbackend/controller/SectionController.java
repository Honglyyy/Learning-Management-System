package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.CourseResponseDTO;
import com.ly.lmsbackend.dto.SectionCreateDTO;
import com.ly.lmsbackend.dto.SectionResponseDTO;
import com.ly.lmsbackend.service.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SectionController {
    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping("/api/sections")
    public ResponseEntity<List<SectionResponseDTO>> getSections(){
        return new ResponseEntity<>(sectionService.getSections(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/sections")
    public ResponseEntity<SectionResponseDTO> addSection(@RequestBody SectionCreateDTO dto){
        return new ResponseEntity<>(sectionService.addSection(dto),HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/sections/{id}")
    public ResponseEntity<String> deleteSection(@PathVariable Long id){
        sectionService.deleteSection(id);
        return new ResponseEntity<>("Section id " + id +" has now deleted!!", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/sections/{id}")
    public ResponseEntity<SectionResponseDTO> updateSection(
            @PathVariable Long id,
            @RequestBody SectionCreateDTO dto){
        return new ResponseEntity<>(sectionService.updateSection(id,dto),HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/sections/instructor/me")
    public ResponseEntity<List<SectionResponseDTO>> getMySections(
            @RequestParam(required = false) Long courseId,
            Authentication authentication
    ) {
        if (courseId != null) {
            return ResponseEntity.ok(
                    sectionService.getSectionByInstructorAndCourse(authentication.getName(), courseId)
            );
        }

        return ResponseEntity.ok(sectionService.getSectionByInstructor(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/sections/instructor/me")
    public ResponseEntity<SectionResponseDTO> createMySection(
            @RequestBody SectionCreateDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                sectionService.addSectionByInstructor(dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/sections/instructor/me/{id}")
    public void deleteMySection(
            @PathVariable Long id,
            Authentication authentication
    ){
        sectionService.deleteMySection(id, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/sections/instructor/me/{id}")
    public ResponseEntity<SectionResponseDTO> updateMySection(
            @PathVariable Long id,
            @RequestBody SectionCreateDTO dto,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                sectionService.updateMySection(
                        id,
                        dto,
                        authentication.getName()
                )
        );
    }
}
