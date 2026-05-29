package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.QuizAttempt;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByEnrollment_EnrollmentIdAndQuiz_QuizId(Long enrollmentId, Long quizId);

    Optional<QuizAttempt> findByUser_EmailAndQuiz_QuizId(String email, Long quizId);

    @Modifying
    @Query("""
            delete from QuizAttempt qa
            where qa.user.id = :userId
               or qa.enrollment.user.id = :userId
               or qa.enrollment.course.instructor.id = :userId
               or qa.quiz.lesson.section.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
