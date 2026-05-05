package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Enrollments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollments, Long> {
    List<Enrollments> findByCourse_CourseId(Long courseId);

    List<Enrollments> findByUser_UserId(Long userId);
}
