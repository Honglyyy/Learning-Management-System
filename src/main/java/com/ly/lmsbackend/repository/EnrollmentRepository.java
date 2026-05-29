package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Enrollments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollments, Long> {
    Optional<Enrollments> findByUser_IdAndCourse_CourseId(Long userId, Long courseId);

    List<Enrollments> findByUser_Email(String email);

    List<Enrollments> findByCourse_CourseId(Long courseId);

    Optional<Enrollments> findByUser_EmailAndCourse_CourseId(String email, Long courseId);

    @Modifying
    @Query("""
            delete from Enrollments e
            where e.user.id = :userId
               or e.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
