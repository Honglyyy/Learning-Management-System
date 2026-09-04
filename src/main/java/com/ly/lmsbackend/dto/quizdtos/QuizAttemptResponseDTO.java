package com.ly.lmsbackend.dto.quizdtos;

import java.sql.Timestamp;

public record QuizAttemptResponseDTO(
        Long attemptId,
        Long quizId,
        Long enrollmentId,
        Double earnedPoints,
        Double totalPoints,
        Integer correctAnswers,
        Integer totalQuestions,
        Timestamp submittedAt,
        Timestamp updatedAt
) {
}
