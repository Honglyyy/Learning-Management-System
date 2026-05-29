package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByEnrollment_EnrollmentIdAndQuiz_QuizId(Long enrollmentId, Long quizId);

    Optional<QuizAttempt> findByUser_EmailAndQuiz_QuizId(String email, Long quizId);
}
