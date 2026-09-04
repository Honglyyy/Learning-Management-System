package com.ly.lmsbackend.dto.quizdtos;

import com.ly.lmsbackend.dto.questiondtos.QuestionDetailDTO;

import java.util.List;

public record QuizDetailDTO(
        Long quizId,
        String title,
        double totalPoints,
        List<QuestionDetailDTO> question
) {
}
