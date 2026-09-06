package com.ly.lmsbackend.dto.lessondtos;

import java.sql.Timestamp;

public record LessonProgressDTO(
        Long lessonId,
        Boolean isCompleted,
        Double lastPlaybackPositionSeconds,
        Timestamp completedAt
) {
}
