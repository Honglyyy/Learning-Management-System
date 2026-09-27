package com.ly.lmsbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ly.lmsbackend.dto.filedtos.FileUploadResponseDTO;
import com.ly.lmsbackend.model.Assignment;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Sections;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.AssignmentRepository;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.LessonRepository;
import com.ly.lmsbackend.repository.SectionRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime",
            "video/x-msvideo"
    );
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov", "avi");
    private static final Set<String> MATERIAL_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed"
    );
    private static final Set<String> MATERIAL_EXTENSIONS = Set.of("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "zip");
    private static final Set<String> ASSIGNMENT_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "text/csv",
            "text/markdown",
            "application/json",
            "application/zip",
            "application/x-zip-compressed",
            "application/x-rar-compressed",
            "application/x-7z-compressed",
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ASSIGNMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "zip", "rar", "7z", "csv", "json", "md", "png", "jpg", "jpeg", "webp"
    );

    private final Cloudinary cloudinary;
    private final long maxImageSize;
    private final long maxVideoSize;
    private final long maxMaterialSize;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final AssignmentRepository assignmentRepository;

    @Autowired
    public FileUploadService(
            Cloudinary cloudinary,
            @Value("${lms.upload.max-image-size:5242880}") long maxImageSize,
            @Value("${lms.upload.max-video-size:524288000}") long maxVideoSize,
            @Value("${lms.upload.max-material-size:52428800}") long maxMaterialSize,
            UserRepository userRepository,
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            SectionRepository sectionRepository,
            AssignmentRepository assignmentRepository
    ) {
        this.cloudinary = cloudinary;
        this.maxImageSize = maxImageSize;
        this.maxVideoSize = maxVideoSize;
        this.maxMaterialSize = maxMaterialSize;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.sectionRepository = sectionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public static String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return "general";
        }
        String clean = input.trim().toLowerCase(Locale.ROOT);
        clean = clean.replaceAll("[^a-z0-9_-]+", "-");
        clean = clean.replaceAll("-+", "-");
        clean = clean.replaceAll("^[-_]+|[-_]+$", "");
        if (clean.isBlank()) {
            return "general";
        }
        return clean;
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String principalName = auth.getName();
            if (userRepository != null && principalName != null) {
                return userRepository.findByEmail(principalName)
                        .map(Users::getUsername)
                        .or(() -> userRepository.findByUsername(principalName).map(Users::getUsername))
                        .orElse(principalName);
            }
            return principalName;
        }
        return null;
    }

    public Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String principalName = auth.getName();
            if (userRepository != null && principalName != null) {
                return userRepository.findByEmail(principalName)
                        .or(() -> userRepository.findByUsername(principalName))
                        .orElse(null);
            }
        }
        return null;
    }

    public FileUploadResponseDTO uploadProfilePhoto(MultipartFile file, String username) {
        String resolvedUser = (username != null && !username.isBlank()) ? username : getCurrentUsername();
        if (resolvedUser == null || resolvedUser.isBlank()) {
            resolvedUser = "default";
        }
        String folder = "profile-picture/" + sanitize(resolvedUser);
        return upload(file, folder, IMAGE_CONTENT_TYPES, IMAGE_EXTENSIONS, maxImageSize, "image");
    }

    public FileUploadResponseDTO uploadProfilePhoto(MultipartFile file) {
        return uploadProfilePhoto(file, null);
    }

    public FileUploadResponseDTO uploadStudentPhoto(MultipartFile file, String username) {
        return uploadProfilePhoto(file, username);
    }

    public FileUploadResponseDTO uploadStudentPhoto(MultipartFile file) {
        return uploadStudentPhoto(file, null);
    }

    public FileUploadResponseDTO uploadInstructorPhoto(MultipartFile file, String username) {
        return uploadProfilePhoto(file, username);
    }

    public FileUploadResponseDTO uploadInstructorPhoto(MultipartFile file) {
        return uploadInstructorPhoto(file, null);
    }

    public FileUploadResponseDTO uploadCourseCover(
            MultipartFile file,
            Long courseId,
            String courseName,
            String username
    ) {
        String resolvedCourseName = courseName;
        String resolvedUsername = username;

        if (courseId != null && courseRepository != null) {
            Courses course = courseRepository.findById(courseId).orElse(null);
            if (course != null) {
                if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
                    resolvedCourseName = course.getTitle();
                }
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && course.getInstructor() != null) {
                    resolvedUsername = course.getInstructor().getUsername();
                }
            }
        }

        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = getCurrentUsername();
        }
        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = "default";
        }
        if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
            resolvedCourseName = "general";
        }

        String folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName);
        return upload(file, folder, IMAGE_CONTENT_TYPES, IMAGE_EXTENSIONS, maxImageSize, "image");
    }

    public FileUploadResponseDTO uploadCourseCover(MultipartFile file) {
        return uploadCourseCover(file, null, null, null);
    }

    public FileUploadResponseDTO uploadLessonVideo(
            MultipartFile file,
            Long lessonId,
            Long sectionId,
            Long courseId,
            String lessonName,
            String courseName,
            String username
    ) {
        String resolvedLessonName = lessonName;
        String resolvedCourseName = courseName;
        String resolvedUsername = username;

        if (lessonId != null && lessonRepository != null) {
            Lessons lesson = lessonRepository.findById(lessonId).orElse(null);
            if (lesson != null) {
                if (resolvedLessonName == null || resolvedLessonName.isBlank()) {
                    resolvedLessonName = lesson.getTitle();
                }
                if (lesson.getSection() != null && lesson.getSection().getCourse() != null) {
                    Courses c = lesson.getSection().getCourse();
                    if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
                        resolvedCourseName = c.getTitle();
                    }
                    if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                        resolvedUsername = c.getInstructor().getUsername();
                    }
                }
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && lesson.getInstructor() != null) {
                    resolvedUsername = lesson.getInstructor().getUsername();
                }
            }
        }

        if (sectionId != null && sectionRepository != null && (resolvedCourseName == null || resolvedCourseName.isBlank())) {
            Sections sec = sectionRepository.findById(sectionId).orElse(null);
            if (sec != null && sec.getCourse() != null) {
                Courses c = sec.getCourse();
                resolvedCourseName = c.getTitle();
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                    resolvedUsername = c.getInstructor().getUsername();
                }
            }
        }

        if (courseId != null && courseRepository != null && (resolvedCourseName == null || resolvedCourseName.isBlank())) {
            Courses c = courseRepository.findById(courseId).orElse(null);
            if (c != null) {
                resolvedCourseName = c.getTitle();
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                    resolvedUsername = c.getInstructor().getUsername();
                }
            }
        }

        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = getCurrentUsername();
        }
        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = "default";
        }
        if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
            resolvedCourseName = "general";
        }
        if (resolvedLessonName == null || resolvedLessonName.isBlank()) {
            resolvedLessonName = "general";
        }

        String folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName) + "/" + sanitize(resolvedLessonName);
        return upload(file, folder, VIDEO_CONTENT_TYPES, VIDEO_EXTENSIONS, maxVideoSize, "video");
    }

    public FileUploadResponseDTO uploadLessonVideo(MultipartFile file) {
        return uploadLessonVideo(file, null, null, null, null, null, null);
    }

    public FileUploadResponseDTO uploadMaterial(
            MultipartFile file,
            Long lessonId,
            Long sectionId,
            Long courseId,
            String lessonName,
            String courseName,
            String username
    ) {
        String resolvedLessonName = lessonName;
        String resolvedCourseName = courseName;
        String resolvedUsername = username;

        if (lessonId != null && lessonRepository != null) {
            Lessons lesson = lessonRepository.findById(lessonId).orElse(null);
            if (lesson != null) {
                if (resolvedLessonName == null || resolvedLessonName.isBlank()) {
                    resolvedLessonName = lesson.getTitle();
                }
                if (lesson.getSection() != null && lesson.getSection().getCourse() != null) {
                    Courses c = lesson.getSection().getCourse();
                    if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
                        resolvedCourseName = c.getTitle();
                    }
                    if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                        resolvedUsername = c.getInstructor().getUsername();
                    }
                }
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && lesson.getInstructor() != null) {
                    resolvedUsername = lesson.getInstructor().getUsername();
                }
            }
        }

        if (sectionId != null && sectionRepository != null && (resolvedCourseName == null || resolvedCourseName.isBlank())) {
            Sections sec = sectionRepository.findById(sectionId).orElse(null);
            if (sec != null && sec.getCourse() != null) {
                Courses c = sec.getCourse();
                resolvedCourseName = c.getTitle();
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                    resolvedUsername = c.getInstructor().getUsername();
                }
            }
        }

        if (courseId != null && courseRepository != null && (resolvedCourseName == null || resolvedCourseName.isBlank())) {
            Courses c = courseRepository.findById(courseId).orElse(null);
            if (c != null) {
                resolvedCourseName = c.getTitle();
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                    resolvedUsername = c.getInstructor().getUsername();
                }
            }
        }

        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = getCurrentUsername();
        }
        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = "default";
        }
        if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
            resolvedCourseName = "general";
        }

        String folder;
        if (resolvedLessonName != null && !resolvedLessonName.isBlank()) {
            folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName) + "/" + sanitize(resolvedLessonName);
        } else {
            folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName) + "/materials";
        }

        return upload(file, folder, MATERIAL_CONTENT_TYPES, MATERIAL_EXTENSIONS, maxMaterialSize, "auto");
    }

    public FileUploadResponseDTO uploadMaterial(MultipartFile file) {
        return uploadMaterial(file, null, null, null, null, null, null);
    }

    public FileUploadResponseDTO uploadAssignmentFile(
            MultipartFile file,
            Long assignmentId,
            Long courseId,
            Long sectionId,
            Long lessonId,
            String assignmentTitle,
            String courseName,
            String lessonName,
            String username
    ) {
        String resolvedCourseName = courseName;
        String resolvedLessonName = lessonName;
        String resolvedUsername = username;
        String currentUsername = getCurrentUsername();
        Users currentUser = getCurrentUser();
        boolean isStudent = currentUser != null && (currentUser.getRole() == Roles.STUDENT || currentUser.getRole() == Roles.USER);

        if (assignmentId != null && assignmentRepository != null) {
            Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
            if (assignment != null) {
                if (assignment.getCourse() != null) {
                    Courses c = assignment.getCourse();
                    if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
                        resolvedCourseName = c.getTitle();
                    }
                    if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                        resolvedUsername = c.getInstructor().getUsername();
                    }
                }
                if (assignment.getSection() != null && assignment.getSection().getCourse() != null) {
                    Courses c = assignment.getSection().getCourse();
                    if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
                        resolvedCourseName = c.getTitle();
                    }
                    if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                        resolvedUsername = c.getInstructor().getUsername();
                    }
                }
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && assignment.getInstructor() != null) {
                    resolvedUsername = assignment.getInstructor().getUsername();
                }
            }
        }

        if (lessonId != null && lessonRepository != null && (resolvedLessonName == null || resolvedLessonName.isBlank())) {
            Lessons l = lessonRepository.findById(lessonId).orElse(null);
            if (l != null) {
                resolvedLessonName = l.getTitle();
                if (l.getSection() != null && l.getSection().getCourse() != null) {
                    Courses c = l.getSection().getCourse();
                    if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
                        resolvedCourseName = c.getTitle();
                    }
                    if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                        resolvedUsername = c.getInstructor().getUsername();
                    }
                }
            }
        }

        if (sectionId != null && sectionRepository != null && (resolvedCourseName == null || resolvedCourseName.isBlank())) {
            Sections sec = sectionRepository.findById(sectionId).orElse(null);
            if (sec != null && sec.getCourse() != null) {
                Courses c = sec.getCourse();
                resolvedCourseName = c.getTitle();
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                    resolvedUsername = c.getInstructor().getUsername();
                }
            }
        }

        if (courseId != null && courseRepository != null && (resolvedCourseName == null || resolvedCourseName.isBlank())) {
            Courses c = courseRepository.findById(courseId).orElse(null);
            if (c != null) {
                resolvedCourseName = c.getTitle();
                if ((resolvedUsername == null || resolvedUsername.isBlank()) && c.getInstructor() != null) {
                    resolvedUsername = c.getInstructor().getUsername();
                }
            }
        }

        if (resolvedUsername == null || resolvedUsername.isBlank()) {
            resolvedUsername = currentUsername != null ? currentUsername : "default";
        }
        if (resolvedCourseName == null || resolvedCourseName.isBlank()) {
            resolvedCourseName = "general";
        }

        String folder;
        if (isStudent) {
            String studentName = currentUsername != null ? currentUsername : "student";
            if (resolvedLessonName != null && !resolvedLessonName.isBlank()) {
                folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName) + "/" + sanitize(resolvedLessonName) + "/assignments/submissions/" + sanitize(studentName);
            } else {
                folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName) + "/assignments/submissions/" + sanitize(studentName);
            }
        } else {
            if (resolvedLessonName != null && !resolvedLessonName.isBlank()) {
                folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName) + "/" + sanitize(resolvedLessonName) + "/assignments";
            } else {
                folder = "course/" + sanitize(resolvedUsername) + "/" + sanitize(resolvedCourseName) + "/assignments";
            }
        }

        return upload(file, folder, ASSIGNMENT_CONTENT_TYPES, ASSIGNMENT_EXTENSIONS, maxMaterialSize, "auto");
    }

    public FileUploadResponseDTO uploadAssignmentFile(MultipartFile file) {
        return uploadAssignmentFile(file, null, null, null, null, null, null, null, null);
    }

    public void deleteAsset(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            Map res = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            if (res == null || !"ok".equals(res.get("result"))) {
                res = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
                if (res == null || !"ok".equals(res.get("result"))) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private FileUploadResponseDTO upload(
            MultipartFile file,
            String folder,
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions,
            long maxSize,
            String resourceType
    ) {
        validateFile(file, allowedContentTypes, allowedExtensions, maxSize);

        String originalFileName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String publicId = UUID.randomUUID().toString();

        String url;
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", resourceType != null ? resourceType : "auto"
            ));
            url = uploadResult.get("secure_url").toString();
            if (uploadResult.get("public_id") != null) {
                publicId = uploadResult.get("public_id").toString();
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file to Cloudinary");
        }

        return new FileUploadResponseDTO(
                originalFileName,
                publicId,
                file.getContentType(),
                file.getSize(),
                url
        );
    }

    private void validateFile(
            MultipartFile file,
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions,
            long maxSize
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File is too large");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!allowedContentTypes.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file content type");
        }

        String originalFileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extension(originalFileName);
        if (!allowedExtensions.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file extension");
        }
    }

    private String extension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File extension is required");
        }
        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    public record OrganizedAsset(String publicId, String url) {}

    public OrganizedAsset organizeCourseCover(String currentPublicId, String username, String courseName) {
        if (currentPublicId == null || currentPublicId.isBlank()) {
            return null;
        }
        String resolvedUser = (username != null && !username.isBlank()) ? username : getCurrentUsername();
        if (resolvedUser == null || resolvedUser.isBlank()) {
            resolvedUser = "default";
        }
        String resolvedCourse = (courseName != null && !courseName.isBlank()) ? courseName : "general";
        String targetFolder = "course/" + sanitize(resolvedUser) + "/" + sanitize(resolvedCourse);
        return organizeAsset(currentPublicId, targetFolder, "image");
    }

    public OrganizedAsset organizeLessonVideo(String currentPublicId, String username, String courseName, String lessonName) {
        if (currentPublicId == null || currentPublicId.isBlank()) {
            return null;
        }
        String resolvedUser = (username != null && !username.isBlank()) ? username : getCurrentUsername();
        if (resolvedUser == null || resolvedUser.isBlank()) {
            resolvedUser = "default";
        }
        String resolvedCourse = (courseName != null && !courseName.isBlank()) ? courseName : "general";
        String resolvedLesson = (lessonName != null && !lessonName.isBlank()) ? lessonName : "general";
        String targetFolder = "course/" + sanitize(resolvedUser) + "/" + sanitize(resolvedCourse) + "/" + sanitize(resolvedLesson);
        return organizeAsset(currentPublicId, targetFolder, "video");
    }

    public OrganizedAsset organizeMaterial(String currentPublicId, String username, String courseName, String lessonName) {
        if (currentPublicId == null || currentPublicId.isBlank()) {
            return null;
        }
        String resolvedUser = (username != null && !username.isBlank()) ? username : getCurrentUsername();
        if (resolvedUser == null || resolvedUser.isBlank()) {
            resolvedUser = "default";
        }
        String resolvedCourse = (courseName != null && !courseName.isBlank()) ? courseName : "general";
        String targetFolder;
        if (lessonName != null && !lessonName.isBlank()) {
            targetFolder = "course/" + sanitize(resolvedUser) + "/" + sanitize(resolvedCourse) + "/" + sanitize(lessonName);
        } else {
            targetFolder = "course/" + sanitize(resolvedUser) + "/" + sanitize(resolvedCourse) + "/materials";
        }
        return organizeAsset(currentPublicId, targetFolder, "auto");
    }

    public OrganizedAsset organizeAssignmentSupportingFile(String currentPublicId, String username, String courseName, String lessonName) {
        if (currentPublicId == null || currentPublicId.isBlank()) {
            return null;
        }
        String resolvedUser = (username != null && !username.isBlank()) ? username : getCurrentUsername();
        if (resolvedUser == null || resolvedUser.isBlank()) {
            resolvedUser = "default";
        }
        String resolvedCourse = (courseName != null && !courseName.isBlank()) ? courseName : "general";
        String targetFolder;
        if (lessonName != null && !lessonName.isBlank()) {
            targetFolder = "course/" + sanitize(resolvedUser) + "/" + sanitize(resolvedCourse) + "/" + sanitize(lessonName) + "/assignments";
        } else {
            targetFolder = "course/" + sanitize(resolvedUser) + "/" + sanitize(resolvedCourse) + "/assignments";
        }
        return organizeAsset(currentPublicId, targetFolder, "auto");
    }

    public OrganizedAsset organizeAssignmentSubmission(String currentPublicId, String instructorUsername, String courseName, String studentUsername) {
        if (currentPublicId == null || currentPublicId.isBlank()) {
            return null;
        }
        String resolvedInstructor = (instructorUsername != null && !instructorUsername.isBlank()) ? instructorUsername : "default";
        String resolvedCourse = (courseName != null && !courseName.isBlank()) ? courseName : "general";
        String resolvedStudent = (studentUsername != null && !studentUsername.isBlank()) ? studentUsername : "student";
        String targetFolder = "course/" + sanitize(resolvedInstructor) + "/" + sanitize(resolvedCourse) + "/assignments/submissions/" + sanitize(resolvedStudent);
        return organizeAsset(currentPublicId, targetFolder, "auto");
    }

    private OrganizedAsset organizeAsset(String currentPublicId, String targetFolder, String resourceType) {
        if (currentPublicId == null || currentPublicId.isBlank() || targetFolder == null || targetFolder.isBlank()) {
            return null;
        }

        int lastSlash = currentPublicId.lastIndexOf('/');
        String baseName = (lastSlash >= 0) ? currentPublicId.substring(lastSlash + 1) : currentPublicId;
        String newPublicId = targetFolder + "/" + baseName;

        if (currentPublicId.equals(newPublicId) || currentPublicId.startsWith(targetFolder + "/")) {
            return null;
        }

        try {
            Map result = cloudinary.uploader().rename(
                    currentPublicId,
                    newPublicId,
                    ObjectUtils.asMap(
                            "resource_type", resourceType != null ? resourceType : "image",
                            "overwrite", true
                    )
            );
            if (result != null && result.get("public_id") != null && result.get("secure_url") != null) {
                return new OrganizedAsset(
                        result.get("public_id").toString(),
                        result.get("secure_url").toString()
                );
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
