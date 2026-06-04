package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Sections;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends JpaRepository<Sections, Long> {
    List<Sections> findByCourse_CourseId(Long courseId);
    List<Sections> findByInstructor_Email(String email);
    List<Sections> findByInstructor_EmailAndCourse_CourseId(String email, Long courseId);

    @Modifying
    @Query("""
            delete from Sections s
            where s.instructor.id = :userId
               or s.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
