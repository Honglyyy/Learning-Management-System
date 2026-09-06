package com.ly.lmsbackend.dto.certificatedtos;

import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record CertificateVerifyDTO(
        Boolean isValid,
        String certificateCode,
        String studentName,
        String courseTitle,
        String instructorName,
        String programDuration,
        Double finalScore,
        Timestamp issuedAt,
        String verificationUrl,
        String issuedBy
) {
}
