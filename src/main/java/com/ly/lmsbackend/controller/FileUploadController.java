package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.filedtos.FileUploadResponseDTO;
import com.ly.lmsbackend.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {
    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping(value = "/api/uploads/course-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadCourseCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "courseName", required = false) String courseName,
            @RequestParam(value = "courseTitle", required = false) String courseTitle,
            @RequestParam(value = "username", required = false) String username
    ) {
        String resolvedCourseName = (courseName != null && !courseName.isBlank()) ? courseName : courseTitle;
        return new ResponseEntity<>(fileUploadService.uploadCourseCover(file, courseId, resolvedCourseName, username), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping(value = "/api/uploads/lesson-video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadLessonVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam(value = "lessonName", required = false) String lessonName,
            @RequestParam(value = "lessonTitle", required = false) String lessonTitle,
            @RequestParam(value = "sectionId", required = false) Long sectionId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "courseName", required = false) String courseName,
            @RequestParam(value = "courseTitle", required = false) String courseTitle,
            @RequestParam(value = "username", required = false) String username
    ) {
        String resolvedLessonName = (lessonName != null && !lessonName.isBlank()) ? lessonName : lessonTitle;
        String resolvedCourseName = (courseName != null && !courseName.isBlank()) ? courseName : courseTitle;
        return new ResponseEntity<>(fileUploadService.uploadLessonVideo(file, lessonId, sectionId, courseId, resolvedLessonName, resolvedCourseName, username), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping(value = "/api/uploads/material", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam(value = "lessonName", required = false) String lessonName,
            @RequestParam(value = "lessonTitle", required = false) String lessonTitle,
            @RequestParam(value = "sectionId", required = false) Long sectionId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "courseName", required = false) String courseName,
            @RequestParam(value = "courseTitle", required = false) String courseTitle,
            @RequestParam(value = "username", required = false) String username
    ) {
        String resolvedLessonName = (lessonName != null && !lessonName.isBlank()) ? lessonName : lessonTitle;
        String resolvedCourseName = (courseName != null && !courseName.isBlank()) ? courseName : courseTitle;
        return new ResponseEntity<>(fileUploadService.uploadMaterial(file, lessonId, sectionId, courseId, resolvedLessonName, resolvedCourseName, username), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT', 'USER')")
    @PostMapping(value = "/api/uploads/assignment-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadAssignmentFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "assignmentId", required = false) Long assignmentId,
            @RequestParam(value = "assignmentTitle", required = false) String assignmentTitle,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "courseName", required = false) String courseName,
            @RequestParam(value = "courseTitle", required = false) String courseTitle,
            @RequestParam(value = "sectionId", required = false) Long sectionId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @RequestParam(value = "lessonName", required = false) String lessonName,
            @RequestParam(value = "lessonTitle", required = false) String lessonTitle,
            @RequestParam(value = "username", required = false) String username
    ) {
        String resolvedCourseName = (courseName != null && !courseName.isBlank()) ? courseName : courseTitle;
        String resolvedLessonName = (lessonName != null && !lessonName.isBlank()) ? lessonName : lessonTitle;
        return new ResponseEntity<>(fileUploadService.uploadAssignmentFile(file, assignmentId, courseId, sectionId, lessonId, assignmentTitle, resolvedCourseName, resolvedLessonName, username), HttpStatus.CREATED);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/api/uploads/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "username", required = false) String username
    ) {
        return new ResponseEntity<>(fileUploadService.uploadProfilePhoto(file, username), HttpStatus.CREATED);
    }
}
