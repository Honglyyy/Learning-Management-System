package com.ly.lmsbackend.dto.activitydtos;

import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record ActivityLogResponseDTO(
        Long id,
        String activityType,
        String description,
        Timestamp timestamp
) {
}
