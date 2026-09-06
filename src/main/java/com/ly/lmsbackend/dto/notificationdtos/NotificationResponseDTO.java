package com.ly.lmsbackend.dto.notificationdtos;

import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record NotificationResponseDTO(
        Long id,
        String title,
        String message,
        String type,
        Boolean isRead,
        String actionUrl,
        Timestamp createdAt
) {
}
