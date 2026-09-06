package com.ly.lmsbackend.dto.lessondtos;

public record LessonStatusDTO(
        Long lessonId,
        String title,
        Integer orderIndex,
        String duration,
        String status,
        Boolean isFree
) {
    public LessonStatusDTO(Long lessonId, String title, Integer orderIndex, String duration, String status) {
        this(lessonId, title, orderIndex, duration, status, false);
    }
}
