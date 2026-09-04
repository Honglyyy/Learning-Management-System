package com.ly.lmsbackend.dto.quizdtos;

import java.util.List;

public record QuizSubmitDTO(
        List<Long> answerIds
) {
}
