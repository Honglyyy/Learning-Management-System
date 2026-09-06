package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.coursedtos.CourseCreateDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseDetailDTO;
import com.ly.lmsbackend.dto.coursedtos.CourseResponseDTO;
import com.ly.lmsbackend.service.CourseService;
import com.ly.lmsbackend.service.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses(){
        return new ResponseEntity<>(courseService.getAllCourses(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/courses")
    public ResponseEntity<CourseResponseDTO> createCourse(@RequestBody CourseCreateDTO dto){
        return new ResponseEntity<>(courseService.addCourse(dto),HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/courses/instructor/me")
    public ResponseEntity<List<CourseResponseDTO>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getCoursesByInstructor(authentication.getName()));
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/api/courses/instructor/me")
    public ResponseEntity<CourseResponseDTO> createMyCourse(
            @RequestBody CourseCreateDTO dto,
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
    ){
        courseService.deleteMyCourse(id,authentication.getName());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/courses/instructor/me/{id}")
    public ResponseEntity<CourseResponseDTO> updateMyCourse(
            @PathVariable Long id,
            @RequestBody CourseCreateDTO dto,
            Authentication authentication
    ){
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
    public ResponseEntity<String> deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
        return new ResponseEntity<>("Course id " + id + " has now deleted!!", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/courses/{id}")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseCreateDTO dto
    ) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto));
    }

    @GetMapping("/api/courses/{id}")
    public ResponseEntity<CourseDetailDTO> getSectionByCourseId(
            @PathVariable Long id
    ){
        return new ResponseEntity<>(courseService.getCourseDetail(id), HttpStatus.OK);
    }
}
