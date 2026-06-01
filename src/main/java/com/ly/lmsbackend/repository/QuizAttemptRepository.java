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
    @Query("delete from QuizAttempt qa where qa.enrollment.enrollmentId = :enrollmentId")
    void deleteAllByEnrollmentId(@Param("enrollmentId") Long enrollmentId);

    @Modifying
    @Query("delete from QuizAttempt qa where qa.quiz.quizId = :quizId")
    void deleteAllByQuizId(@Param("quizId") Long quizId);

    @Modifying
    @Query("""
            delete from QuizAttempt qa
            where qa.enrollment.course.courseId = :courseId
               or qa.quiz.lesson.section.course.courseId = :courseId
            """)
    void deleteAllByCourseId(@Param("courseId") Long courseId);

    @Modifying
    @Query("delete from QuizAttempt qa where qa.quiz.lesson.section.sectionId = :sectionId")
    void deleteAllBySectionId(@Param("sectionId") Long sectionId);

    @Modifying
    @Query("delete from QuizAttempt qa where qa.quiz.lesson.lessonId = :lessonId")
    void deleteAllByLessonId(@Param("lessonId") Long lessonId);

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
