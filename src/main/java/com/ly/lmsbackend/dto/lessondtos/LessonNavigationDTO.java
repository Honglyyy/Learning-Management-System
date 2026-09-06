package com.ly.lmsbackend.dto.lessondtos;

public record LessonNavigationDTO(
        Long currentLessonId,
        Long previousLessonId,
        Long nextLessonId,
        Boolean hasPrevious,
        Boolean hasNext
) {
}
