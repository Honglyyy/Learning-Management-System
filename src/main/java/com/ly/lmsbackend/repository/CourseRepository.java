package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Courses, Long> {
    List<Courses> findByCategories_CategoryId(Long categoryId);
    List<Courses> findByInstructor_Email(String email);
    List<Courses> findByInstructor_Id(Long id);
}
