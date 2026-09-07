package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.dashboarddtos.AdminDashboardDTO;
import com.ly.lmsbackend.dto.dashboarddtos.InstructorDashboardDTO;
import com.ly.lmsbackend.dto.dashboarddtos.StudentDashboardDTO;
import com.ly.lmsbackend.dto.progressdtos.ContinueLearningDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final PaymentRepository paymentRepository;
    private final ProgressService progressService;

    @Transactional(readOnly = true)
    public StudentDashboardDTO getStudentDashboard(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Students student = studentRepository.findByUser_Email(email).orElse(null);

        List<Enrollments> enrollments = enrollmentRepository.findByUser_Email(email);
        long totalEnrolled = enrollments.size();
        long completedCourses = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();
        long inProgressCourses = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();

        List<QuizAttempt> attempts = quizAttemptRepository.findByUser_Email(email);
        double avgQuizScore = 0.0;
        if (!attempts.isEmpty()) {
            double sumPercentage = 0.0;
            int validCount = 0;
            for (QuizAttempt attempt : attempts) {
                if (attempt.getTotalPoints() != null && attempt.getTotalPoints() > 0 && attempt.getEarnedPoints() != null) {
                    sumPercentage += (attempt.getEarnedPoints() / attempt.getTotalPoints()) * 100.0;
                    validCount++;
                } else if (attempt.getTotalQuestions() != null && attempt.getTotalQuestions() > 0 && attempt.getCorrectAnswers() != null) {
                    sumPercentage += (attempt.getCorrectAnswers().doubleValue() / attempt.getTotalQuestions()) * 100.0;
                    validCount++;
                }
            }
            if (validCount > 0) {
                avgQuizScore = Math.round((sumPercentage / validCount) * 100.0) / 100.0;
            }
        }

        Double totalEarnedPoints = student != null && student.getTotalPoints() != null
                ? student.getTotalPoints()
                : enrollments.stream().mapToDouble(e -> e.getEarnedPoints() != null ? e.getEarnedPoints() : 0.0).sum();

        ContinueLearningDTO continueLearning = null;
        try {
            continueLearning = progressService.getContinueLearning(email);
        } catch (Exception ignored) {
        }

        return StudentDashboardDTO.builder()
                .totalEnrolledCourses(totalEnrolled)
                .inProgressCourses(inProgressCourses)
                .completedCourses(completedCourses)
                .averageQuizScore(avgQuizScore)
                .totalEarnedPoints(totalEarnedPoints)
                .continueLearning(continueLearning)
                .build();
    }

    @Transactional(readOnly = true)
    public InstructorDashboardDTO getInstructorDashboard(String email) {
        Users instructorUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instructor not found"));

        List<Courses> courses = courseRepository.findByInstructor_Email(email);
        long totalCoursesCreated = courses.size();
        List<Long> courseIds = courses.stream().map(Courses::getCourseId).toList();

        if (courseIds.isEmpty()) {
            Instructors instructorProfile = instructorRepository.findByUser_Email(email).orElse(null);
            Double defaultRating = instructorProfile != null && instructorProfile.getAverageRating() != null
                    ? instructorProfile.getAverageRating()
                    : 5.0;
            return InstructorDashboardDTO.builder()
                    .totalCoursesCreated(0)
                    .activeStudentCount(0)
                    .totalEnrollments(0)
                    .pendingSubmissionsToGrade(0)
                    .averageCourseRating(defaultRating)
                    .build();
        }

        List<Enrollments> enrollments = enrollmentRepository.findByCourse_CourseIdIn(courseIds);
        long totalEnrollments = enrollments.size();
        long activeStudentCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE && e.getUser() != null)
                .map(e -> e.getUser().getId())
                .distinct()
                .count();

        long pendingSubmissions = assignmentSubmissionRepository.countByAssignment_Course_CourseIdInAndStatusIn(
                courseIds,
                List.of(AssignmentStatus.SUBMITTED, AssignmentStatus.LATE)
        );

        List<CourseReviews> reviews = courseReviewRepository.findByCourse_CourseIdIn(courseIds);
        Double avgRating = reviews.isEmpty()
                ? 5.0
                : Math.round(reviews.stream().mapToInt(CourseReviews::getRating).average().orElse(5.0) * 10.0) / 10.0;

        return InstructorDashboardDTO.builder()
                .totalCoursesCreated(totalCoursesCreated)
                .activeStudentCount(activeStudentCount)
                .totalEnrollments(totalEnrollments)
                .pendingSubmissionsToGrade(pendingSubmissions)
                .averageCourseRating(avgRating)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO getAdminDashboard() {
        long totalStudents = studentRepository.count();
        long studentUsers = userRepository.countByRoleIn(List.of(Roles.STUDENT, Roles.USER));
        totalStudents = Math.max(totalStudents, studentUsers);

        long totalInstructors = instructorRepository.count();
        long instructorUsers = userRepository.countByRole(Roles.INSTRUCTOR);
        totalInstructors = Math.max(totalInstructors, instructorUsers);

        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long completedEnrollments = enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED);

        Double completionRate = totalEnrollments > 0
                ? Math.round((completedEnrollments * 100.0 / totalEnrollments) * 100.0) / 100.0
                : 0.0;

        BigDecimal grossRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.PAID);
        if (grossRevenue == null) {
            grossRevenue = BigDecimal.ZERO;
        }

        long totalQuizzes = quizRepository.count();
        long totalAssignments = assignmentRepository.count();

        return AdminDashboardDTO.builder()
                .totalStudents(totalStudents)
                .totalInstructors(totalInstructors)
                .totalCourses(totalCourses)
                .totalEnrollments(totalEnrollments)
                .completedEnrollments(completedEnrollments)
                .completionRate(completionRate)
                .grossRevenue(grossRevenue)
                .totalQuizzes(totalQuizzes)
                .totalAssignments(totalAssignments)
                .build();
    }
}
