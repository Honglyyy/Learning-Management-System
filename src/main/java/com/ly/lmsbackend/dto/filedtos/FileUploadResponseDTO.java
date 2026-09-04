package com.ly.lmsbackend.dto.filedtos;

public record FileUploadResponseDTO(
        String originalFileName,
        String publicId,
        String contentType,
        long size,
        String url
) {
}
