package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Courses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Courses, Long> {
    List<Courses> findByCategories_CategoryId(Long categoryId);
}
