package com.ly.lmsbackend.dto.certificatedtos;

import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record CertificateResponseDTO(
        Long certificateId,
        String certificateCode,
        Long courseId,
        String courseTitle,
        Long userId,
        String studentName,
        String studentEmail,
        String instructorName,
        Double finalScore,
        String certificateUrl,
        Timestamp issuedAt
) {
}
