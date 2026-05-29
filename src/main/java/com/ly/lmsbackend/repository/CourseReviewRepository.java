package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.CourseReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReviews, Long> {
    List<CourseReviews> findByCourse_CourseId(Long courseId);
    List<CourseReviews> findByCourseCourseId(Long courseId);

    @Modifying
    @Query("""
            delete from CourseReviews cr
            where cr.user.id = :userId
               or cr.course.instructor.id = :userId
            """)
    void deleteAllForUserRemoval(@Param("userId") Long userId);
}
