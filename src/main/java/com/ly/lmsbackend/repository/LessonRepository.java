package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Lessons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lessons, Long> {
    List<Lessons> findByInstructor_Email(String email);

    @Modifying
    @Query("""
            delete from Lessons l
            where l.instructor.id = :userId
               or l.section.instructor.id = :userId
               or l.section.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
