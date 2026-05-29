package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Answers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answers, Long> {
    @Modifying
    @Query("""
            delete from Answers a
            where a.instructor.id = :userId
               or a.question.instructor.id = :userId
               or a.question.quiz.instructor.id = :userId
               or a.question.quiz.lesson.instructor.id = :userId
               or a.question.quiz.lesson.section.instructor.id = :userId
               or a.question.quiz.lesson.section.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
