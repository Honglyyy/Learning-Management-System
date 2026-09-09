package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.lessondtos.LessonCreateDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonQuizDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonResponseDTO;
import com.ly.lmsbackend.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LessonController {


    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/api/lessons")
    public ResponseEntity<List<LessonResponseDTO>> getLessons(){
        return new ResponseEntity<>(lessonService.getLessons(), HttpStatus.OK);
    }

    @GetMapping("/api/lessons/{id}")
    public ResponseEntity<LessonQuizDTO> getLesson(@PathVariable Long id){
        return new ResponseEntity<>(lessonService.getLesson(id), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/lessons")
    public ResponseEntity<LessonResponseDTO> addLesson(@Valid @RequestBody LessonCreateDTO dto){
        return new ResponseEntity<>(lessonService.addLesson(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/lessons/{id}")
    public ResponseEntity<LessonResponseDTO> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody LessonCreateDTO dto
    ){
        return new ResponseEntity<>(lessonService.updateLesson(id,dto), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/lessons/{id}")
    public ResponseEntity<String> deleteLesson(@PathVariable Long id){
        lessonService.deleteLesson(id);
        return new ResponseEntity<>("Lesson id " + id + " has now deleted!!", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/lessons/instructor/me")
    public ResponseEntity<List<LessonResponseDTO>> getMyLessonsByInstructor(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long courseId,
            Authentication authentication
    ) {
        if (sectionId != null) {
            return ResponseEntity.ok(
                    lessonService.getLessonByInstructorAndSection(authentication.getName(), sectionId)
            );
        }

        if (courseId != null) {
            return ResponseEntity.ok(
                    lessonService.getLessonByInstructorAndCourse(authentication.getName(), courseId)
            );
        }

        return ResponseEntity.ok(lessonService.getLessonByInstructor(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/lessons/instructor/me")
    public ResponseEntity<LessonResponseDTO> createMyLesson(
            @Valid @RequestBody LessonCreateDTO dto,
            Authentication authentication
    ) {
        return new ResponseEntity<>(
                lessonService.addLessonByInstructor(dto, authentication.getName()),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/lessons/instructor/me/{id}")
    public void deleteMyLesson(
            @PathVariable Long id,
            Authentication authentication
    ){
        lessonService.deleteMyLesson(id, authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/lessons/instructor/me/{id}")
    public ResponseEntity<LessonResponseDTO> updateMyLesson(
            @PathVariable Long id,
            @Valid @RequestBody LessonCreateDTO dto,
            Authentication authentication
    ){
        return ResponseEntity.ok(
                lessonService.updateMyLesson(
                        id,
                        dto,
                        authentication.getName()
                )
        );
    }
}
