package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.CourseReviews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReviews, Long> {
    List<CourseReviews> findByCourse_CourseId(Long courseId);
    List<CourseReviews> findByCourseCourseId(Long courseId);
}
