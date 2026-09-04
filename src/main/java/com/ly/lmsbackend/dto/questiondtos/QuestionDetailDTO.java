package com.ly.lmsbackend.dto.questiondtos;

import com.ly.lmsbackend.dto.answerdtos.AnswerDetailDTO;

import java.util.List;

public record QuestionDetailDTO(
        Long questionId,
        String questionText,
        Long point,
        List<AnswerDetailDTO> answers
) {
}
