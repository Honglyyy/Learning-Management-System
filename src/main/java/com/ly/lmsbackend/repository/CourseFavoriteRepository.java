package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.CourseFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseFavoriteRepository extends JpaRepository<CourseFavorite, Long> {
    Optional<CourseFavorite> findByUser_EmailAndCourse_CourseId(String email, Long courseId);

    Optional<CourseFavorite> findByUser_IdAndCourse_CourseId(Long userId, Long courseId);

    List<CourseFavorite> findByUser_EmailOrderByCreatedAtDesc(String email);

    List<CourseFavorite> findByUser_IdOrderByCreatedAtDesc(Long userId);

    boolean existsByUser_EmailAndCourse_CourseId(String email, Long courseId);

    boolean existsByUser_IdAndCourse_CourseId(Long userId, Long courseId);

    @Modifying
    @Query("DELETE FROM CourseFavorite cf WHERE cf.user.email = :email AND cf.course.courseId = :courseId")
    void deleteByUser_EmailAndCourse_CourseId(@Param("email") String email, @Param("courseId") Long courseId);

    long countByCourse_CourseId(Long courseId);
}
