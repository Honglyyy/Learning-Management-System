package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.lessondtos.LessonMaterialCreateDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonMaterialDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LessonMaterialService {

    private final LessonMaterialRepository lessonMaterialRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    public LessonMaterialService(
            LessonMaterialRepository lessonMaterialRepository,
            LessonRepository lessonRepository,
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            FileUploadService fileUploadService
    ) {
        this.lessonMaterialRepository = lessonMaterialRepository;
        this.lessonRepository = lessonRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional(readOnly = true)
    public List<LessonMaterialDTO> getMaterials(Long lessonId, String userEmail) {
        Lessons lesson = getLesson(lessonId);

        // If lesson is free preview, materials are accessible without enrollment
        if (!Boolean.TRUE.equals(lesson.getIsFree())) {
            if (userEmail == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required to access lesson materials");
            }

            Users user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // If student, check enrollment
            if (user.getRole() == Roles.STUDENT) {
                Students student = studentRepository.findByUser_Email(userEmail)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
                verifyStudentEnrolledInLesson(student, lesson);
            }
        }

        return lessonMaterialRepository.findByLesson_LessonId(lessonId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public LessonMaterialDTO attachMaterial(Long lessonId, LessonMaterialCreateDTO dto, String userEmail) {
        if (dto.title() == null || dto.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Material title is required");
        }
        if (dto.fileUrl() == null || dto.fileUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Material file URL is required");
        }

        Lessons lesson = getLesson(lessonId);
        verifyCanManageLesson(lesson, userEmail);

        String filePublicId = dto.filePublicId();
        String fileUrl = dto.fileUrl().trim();

        if (fileUploadService != null && filePublicId != null && !filePublicId.isBlank()) {
            String instructorUsername = null;
            if (lesson.getInstructor() != null) {
                instructorUsername = lesson.getInstructor().getUsername();
            } else if (lesson.getSection() != null && lesson.getSection().getInstructor() != null) {
                instructorUsername = lesson.getSection().getInstructor().getUsername();
            } else if (lesson.getSection() != null && lesson.getSection().getCourse() != null && lesson.getSection().getCourse().getInstructor() != null) {
                instructorUsername = lesson.getSection().getCourse().getInstructor().getUsername();
            }
            String courseTitle = (lesson.getSection() != null && lesson.getSection().getCourse() != null)
                    ? lesson.getSection().getCourse().getTitle()
                    : null;
            var organized = fileUploadService.organizeMaterial(filePublicId, instructorUsername, courseTitle, lesson.getTitle());
            if (organized != null) {
                filePublicId = organized.publicId();
                fileUrl = organized.url();
            }
        }

        LessonMaterial material = LessonMaterial.builder()
                .lesson(lesson)
                .title(dto.title().trim())
                .fileUrl(fileUrl)
                .filePublicId(filePublicId)
                .fileType(dto.fileType() != null ? dto.fileType().toUpperCase() : "DOC")
                .fileSize(dto.fileSize())
                .build();

        LessonMaterial saved = lessonMaterialRepository.save(material);
        return toDTO(saved);
    }

    @Transactional
    public void deleteMaterial(Long materialId, String userEmail) {
        LessonMaterial material = lessonMaterialRepository.findById(materialId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material not found with id: " + materialId));

        verifyCanManageLesson(material.getLesson(), userEmail);

        if (material.getFilePublicId() != null && !material.getFilePublicId().isBlank()) {
            fileUploadService.deleteAsset(material.getFilePublicId());
        }

        lessonMaterialRepository.delete(material);
    }

    private Lessons getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found with id: " + lessonId));
    }

    private void verifyCanManageLesson(Lessons lesson, String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == Roles.ADMIN) {
            return;
        }

        Users owner = lesson.getInstructor();
        if (owner == null && lesson.getSection() != null) {
            owner = lesson.getSection().getInstructor();
            if (owner == null && lesson.getSection().getCourse() != null) {
                owner = lesson.getSection().getCourse().getInstructor();
            }
        }

        if (owner == null || !owner.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage materials for this lesson");
        }
    }

    private void verifyStudentEnrolledInLesson(Students student, Lessons lesson) {
        if (Boolean.TRUE.equals(lesson.getIsFree())) {
            return;
        }
        if (lesson.getSection() == null || lesson.getSection().getCourse() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson is not assigned to a valid section/course");
        }
        Long courseId = lesson.getSection().getCourse().getCourseId();

        Users user = student.getUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account not linked to student");
        }

        Enrollments enrollment = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                .orElse(null);

        if (enrollment == null || enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: student is not actively enrolled in this course");
        }

        if (enrollment.isExpired()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: course enrollment has expired. Please renew your access.");
        }
    }

    private LessonMaterialDTO toDTO(LessonMaterial material) {
        return new LessonMaterialDTO(
                material.getMaterialId(),
                material.getLesson().getLessonId(),
                material.getTitle(),
                material.getFileUrl(),
                material.getFileType(),
                material.getFileSize()
        );
    }
}
