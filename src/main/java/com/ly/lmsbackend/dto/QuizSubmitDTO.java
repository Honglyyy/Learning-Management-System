package com.ly.lmsbackend.dto;

import java.util.List;

public record QuizSubmitDTO(
        List<Long> answerIds
) {
}
