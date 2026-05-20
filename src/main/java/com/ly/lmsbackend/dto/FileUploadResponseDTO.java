package com.ly.lmsbackend.dto;

public record FileUploadResponseDTO(
        String originalFileName,
        String fileName,
        String contentType,
        long size,
        String url
) {
}
