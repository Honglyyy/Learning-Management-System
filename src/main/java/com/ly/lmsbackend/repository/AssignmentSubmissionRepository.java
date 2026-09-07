package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignment_AssignmentIdOrderBySubmittedAtDesc(Long assignmentId);
    Optional<AssignmentSubmission> findByAssignment_AssignmentIdAndStudent_Id(Long assignmentId, Long studentId);
    List<AssignmentSubmission> findByStudent_Id(Long studentId);
    List<AssignmentSubmission> findByStudent_Email(String studentEmail);
    long countByAssignment_AssignmentId(Long assignmentId);
    List<AssignmentSubmission> findByAssignment_Course_CourseIdIn(List<Long> courseIds);
    List<AssignmentSubmission> findByAssignment_Course_CourseId(Long courseId);
    long countByAssignment_Course_CourseIdInAndStatusIn(List<Long> courseIds, List<com.ly.lmsbackend.model.AssignmentStatus> statuses);
    long countByAssignment_Instructor_EmailAndStatusIn(String instructorEmail, List<com.ly.lmsbackend.model.AssignmentStatus> statuses);
    long countByStatus(com.ly.lmsbackend.model.AssignmentStatus status);
}
