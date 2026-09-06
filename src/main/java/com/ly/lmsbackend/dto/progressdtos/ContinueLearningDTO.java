package com.ly.lmsbackend.dto.progressdtos;

import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record ContinueLearningDTO(
        Long courseId,
        String courseTitle,
        Long nextLessonId,
        String nextLessonTitle,
        Integer nextLessonOrderIndex,
        Double progressPercentage,
        Double lastPlaybackPositionSeconds,
        Timestamp lastActivityAt
) {
}
