package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Quizzes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quizzes, Long> {
    List<Quizzes> findByLesson_LessonId(Long lessonId);

    @Modifying
    @Query("""
            delete from Quizzes q
            where q.instructor.id = :userId
               or q.lesson.instructor.id = :userId
               or q.lesson.section.instructor.id = :userId
               or q.lesson.section.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
