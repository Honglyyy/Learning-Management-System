package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Questions, Long> {
    @Modifying
    @Query("""
            delete from Questions q
            where q.instructor.id = :userId
               or q.quiz.instructor.id = :userId
               or q.quiz.lesson.instructor.id = :userId
               or q.quiz.lesson.section.instructor.id = :userId
               or q.quiz.lesson.section.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
