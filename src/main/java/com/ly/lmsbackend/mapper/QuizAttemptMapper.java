package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.QuizAttemptResponseDTO;
import com.ly.lmsbackend.model.QuizAttempt;
import org.springframework.stereotype.Component;

@Component
public class QuizAttemptMapper {
    public QuizAttemptResponseDTO toDto(QuizAttempt attempt) {
        return new QuizAttemptResponseDTO(
                attempt.getAttemptId(),
                attempt.getQuiz().getQuizId(),
                attempt.getEnrollment().getEnrollmentId(),
                attempt.getEarnedPoints(),
                attempt.getTotalPoints(),
                attempt.getCorrectAnswers(),
                attempt.getTotalQuestions(),
                attempt.getSubmittedAt(),
                attempt.getUpdatedAt()
        );
    }
}
