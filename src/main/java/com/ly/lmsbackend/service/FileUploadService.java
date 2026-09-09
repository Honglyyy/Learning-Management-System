package com.ly.lmsbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ly.lmsbackend.dto.filedtos.FileUploadResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    public FileUploadService(
            Cloudinary cloudinary,
            @Value("${lms.upload.max-image-size:5242880}") long maxImageSize,
            @Value("${lms.upload.max-video-size:524288000}") long maxVideoSize,
            @Value("${lms.upload.max-material-size:52428800}") long maxMaterialSize
    ) {
        this.cloudinary = cloudinary;
        this.maxImageSize = maxImageSize;
        this.maxVideoSize = maxVideoSize;
        this.maxMaterialSize = maxMaterialSize;
    }

    public FileUploadResponseDTO uploadAssignmentFile(MultipartFile file) {
        return upload(file, "assignment-files", ASSIGNMENT_CONTENT_TYPES, ASSIGNMENT_EXTENSIONS, maxMaterialSize);
    }

    public FileUploadResponseDTO uploadMaterial(MultipartFile file) {
        return upload(file, "lesson-materials", MATERIAL_CONTENT_TYPES, MATERIAL_EXTENSIONS, maxMaterialSize);
    }

    public FileUploadResponseDTO uploadCourseCover(MultipartFile file) {
        return upload(file, "course-covers", IMAGE_CONTENT_TYPES, IMAGE_EXTENSIONS, maxImageSize);
    }

    public FileUploadResponseDTO uploadLessonVideo(MultipartFile file) {
        return upload(file, "lesson-videos", VIDEO_CONTENT_TYPES, VIDEO_EXTENSIONS, maxVideoSize);
    }

    public FileUploadResponseDTO uploadStudentPhoto(MultipartFile file) {
        return upload(file, "profile-photos/students", IMAGE_CONTENT_TYPES, IMAGE_EXTENSIONS, maxImageSize);
    }

    public FileUploadResponseDTO uploadInstructorPhoto(MultipartFile file) {
        return upload(file, "profile-photos/instructors", IMAGE_CONTENT_TYPES, IMAGE_EXTENSIONS, maxImageSize);
    }

    public FileUploadResponseDTO uploadProfilePhoto(MultipartFile file) {
        return upload(file, "profile-photos", IMAGE_CONTENT_TYPES, IMAGE_EXTENSIONS, maxImageSize);
    }

    public void deleteAsset(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException ignored) {
        }
    }

    private FileUploadResponseDTO upload(
            MultipartFile file,
            String folder,
            Set<String> allowedContentTypes,
            Set<String> allowedExtensions,
            long maxSize
    ) {
        validateFile(file, allowedContentTypes, allowedExtensions, maxSize);

        String originalFileName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String extension = extension(originalFileName);
        String publicId = UUID.randomUUID().toString();
        
        String url;
        try {
            String resourceType = folder.contains("video") ? "video" : folder.contains("material") ? "auto" : "image";
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", resourceType
            ));
            url = uploadResult.get("secure_url").toString();
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
}
