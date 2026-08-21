package com.ly.lmsbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ly.lmsbackend.dto.FileUploadResponseDTO;
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

    private final Cloudinary cloudinary;
    private final long maxImageSize;
    private final long maxVideoSize;

    public FileUploadService(
            Cloudinary cloudinary,
            @Value("${lms.upload.max-image-size:5242880}") long maxImageSize,
            @Value("${lms.upload.max-video-size:524288000}") long maxVideoSize
    ) {
        this.cloudinary = cloudinary;
        this.maxImageSize = maxImageSize;
        this.maxVideoSize = maxVideoSize;
    }

    public FileUploadResponseDTO uploadCourseCover(MultipartFile file) {
        return upload(file, "course-covers", IMAGE_CONTENT_TYPES, IMAGE_EXTENSIONS, maxImageSize);
    }

    public FileUploadResponseDTO uploadLessonVideo(MultipartFile file) {
        return upload(file, "lesson-videos", VIDEO_CONTENT_TYPES, VIDEO_EXTENSIONS, maxVideoSize);
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
            String resourceType = folder.contains("video") ? "video" : "image";
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
