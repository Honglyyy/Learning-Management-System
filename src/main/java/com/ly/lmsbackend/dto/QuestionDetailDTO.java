package com.ly.lmsbackend.dto;

import java.util.List;

public record QuestionDetailDTO(
        Long questionId,
        String questionText,
        Long point,
        List<AnswerDetailDTO> answers
) {
}
