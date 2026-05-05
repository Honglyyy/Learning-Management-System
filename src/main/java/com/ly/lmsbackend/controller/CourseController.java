package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.CourseCreateDTO;
import com.ly.lmsbackend.dto.CourseDetailDTO;
import com.ly.lmsbackend.dto.CourseResponseDTO;
import com.ly.lmsbackend.service.CourseService;
import com.ly.lmsbackend.service.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/api/courses")
    public ResponseEntity<CourseResponseDTO> createCourse(@RequestBody CourseCreateDTO dto){
        return new ResponseEntity<>(courseService.addCourse(dto),HttpStatus.CREATED);
    }

    @DeleteMapping("/api/courses/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id){
        courseService.deleteCourse(id);
        return new ResponseEntity<>("Course id " + id + " has now deleted!!", HttpStatus.OK);
    }

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
