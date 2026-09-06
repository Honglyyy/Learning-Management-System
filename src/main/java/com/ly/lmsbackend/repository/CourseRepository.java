package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.CourseStatus;
import com.ly.lmsbackend.model.Courses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Courses, Long>, JpaSpecificationExecutor<Courses> {
    List<Courses> findByCategories_CategoryId(Long categoryId);
    List<Courses> findByInstructor_Email(String email);
    List<Courses> findByInstructor_Id(Long id);
    List<Courses> findByStatus(CourseStatus status);

    @Query("SELECT c FROM Courses c LEFT JOIN c.enrollments e WHERE (c.status = 'PUBLISHED' OR c.status IS NULL) GROUP BY c ORDER BY COUNT(e) DESC")
    List<Courses> findPopularCourses();
}

