package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourse_CourseIdOrderByCreatedAtDesc(Long courseId);
    List<Assignment> findByCourse_CourseId(Long courseId);
    List<Assignment> findByCourse_CourseIdAndSection_SectionId(Long courseId, Long sectionId);
    List<Assignment> findByInstructor_Id(Long instructorId);
    List<Assignment> findByCourse_Instructor_Email(String instructorEmail);
}
