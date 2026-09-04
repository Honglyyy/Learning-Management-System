package com.ly.lmsbackend.dto.lessondtos;

import com.ly.lmsbackend.dto.quizdtos.QuizDetailDTO;

import java.util.List;

public record LessonQuizDTO(
        Long lessonId,
        String title,
        String videoUrl,
        String videoPublicId,
        List<QuizDetailDTO> quizz
) {
}
