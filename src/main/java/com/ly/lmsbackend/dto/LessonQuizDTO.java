package com.ly.lmsbackend.dto;

import java.util.List;

public record LessonQuizDTO(
        Long lessonId,
        String title,
        String videoDir,
        List<QuizDetailDTO> quizz
) {
}
