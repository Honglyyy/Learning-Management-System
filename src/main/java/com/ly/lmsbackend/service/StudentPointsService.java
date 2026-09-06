package com.ly.lmsbackend.service;

import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentPointsService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final QuizRepository quizRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    public StudentPointsService(
            QuizAttemptRepository quizAttemptRepository,
            AssignmentSubmissionRepository submissionRepository,
            QuizRepository quizRepository,
            AssignmentRepository assignmentRepository,
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository
    ) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.submissionRepository = submissionRepository;
        this.quizRepository = quizRepository;
        this.assignmentRepository = assignmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public void recalculateCourseAndStudentPoints(Users user, Courses course) {
        if (user == null || course == null) {
            return;
        }

        Enrollments enrollment = enrollmentRepository
                .findByUser_IdAndCourse_CourseId(user.getId(), course.getCourseId())
                .orElse(null);

        recalculateCourseAndStudentPoints(user, course, enrollment);
    }

    @Transactional
    public void recalculateCourseAndStudentPoints(Users user, Courses course, Enrollments enrollment) {
        if (user == null) {
            return;
        }

        // 1. Calculate points for this specific course enrollment if present
        if (enrollment != null && course != null) {
            // Earned points from quizzes in this course
            List<QuizAttempt> courseAttempts = quizAttemptRepository
                    .findAll()
                    .stream()
                    .filter(qa -> qa.getUser() != null
                            && qa.getUser().getId().equals(user.getId())
                            && qa.getQuiz() != null
                            && qa.getQuiz().getLesson() != null
                            && qa.getQuiz().getLesson().getSection() != null
                            && qa.getQuiz().getLesson().getSection().getCourse() != null
                            && qa.getQuiz().getLesson().getSection().getCourse().getCourseId().equals(course.getCourseId()))
                    .toList();

            double quizEarned = courseAttempts.stream()
                    .mapToDouble(qa -> qa.getEarnedPoints() != null ? qa.getEarnedPoints() : 0.0)
                    .sum();

            // Total possible points from quizzes in this course
            List<Quizzes> courseQuizzes = quizRepository.findAll()
                    .stream()
                    .filter(q -> q.getLesson() != null
                            && q.getLesson().getSection() != null
                            && q.getLesson().getSection().getCourse() != null
                            && q.getLesson().getSection().getCourse().getCourseId().equals(course.getCourseId()))
                    .toList();

            double quizTotal = courseQuizzes.stream()
                    .mapToDouble(q -> q.getTotalPoint() != null ? q.getTotalPoint() : 0.0)
                    .sum();

            // Earned points from assignments in this course
            List<AssignmentSubmission> courseSubmissions = submissionRepository
                    .findByStudent_Id(user.getId())
                    .stream()
                    .filter(sub -> sub.getAssignment() != null
                            && sub.getAssignment().getCourse() != null
                            && sub.getAssignment().getCourse().getCourseId().equals(course.getCourseId()))
                    .toList();

            double assignmentEarned = courseSubmissions.stream()
                    .mapToDouble(sub -> sub.getScore() != null ? sub.getScore() : 0.0)
                    .sum();

            // Total possible points from assignments in this course
            List<Assignment> courseAssignments = assignmentRepository
                    .findByCourse_CourseIdOrderByCreatedAtDesc(course.getCourseId());

            double assignmentTotal = courseAssignments.stream()
                    .mapToDouble(a -> a.getMaxScore() != null ? a.getMaxScore() : 100.0)
                    .sum();

            double totalEarned = Math.round((quizEarned + assignmentEarned) * 100.0) / 100.0;
            double totalPossible = Math.round((quizTotal + assignmentTotal) * 100.0) / 100.0;

            enrollment.setEarnedPoints(totalEarned);
            enrollment.setTotalPoints(totalPossible);
            enrollmentRepository.save(enrollment);
        }

        // 2. Calculate overall total points for the student across all courses
        syncGlobalStudentPoints(user);
    }

    @Transactional
    public void syncGlobalStudentPoints(Users user) {
        if (user == null) {
            return;
        }

        Students student = studentRepository.findByUser_Id(user.getId()).orElse(null);
        if (student == null) {
            return;
        }

        // All quiz earned points by this user
        double totalQuizPoints = quizAttemptRepository.findAll()
                .stream()
                .filter(qa -> qa.getUser() != null && qa.getUser().getId().equals(user.getId()))
                .mapToDouble(qa -> qa.getEarnedPoints() != null ? qa.getEarnedPoints() : 0.0)
                .sum();

        // All assignment scores by this user
        double totalAssignmentPoints = submissionRepository.findByStudent_Id(user.getId())
                .stream()
                .mapToDouble(sub -> sub.getScore() != null ? sub.getScore() : 0.0)
                .sum();

        double globalTotalPoints = Math.round((totalQuizPoints + totalAssignmentPoints) * 100.0) / 100.0;
        student.setTotalPoints(globalTotalPoints);
        studentRepository.save(student);
    }
}
