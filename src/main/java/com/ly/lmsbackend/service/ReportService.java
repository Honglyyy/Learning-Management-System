package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.reportdtos.*;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional(readOnly = true)
    public EnrollmentReportDTO getEnrollmentReport(String email) {
        Users user = getUserByEmail(email);

        List<Courses> courses;
        List<Enrollments> enrollments;

        if (user.getRole() == Roles.ADMIN) {
            courses = courseRepository.findAll();
            enrollments = enrollmentRepository.findAll();
        } else {
            courses = courseRepository.findByInstructor_Email(email);
            List<Long> courseIds = courses.stream().map(Courses::getCourseId).toList();
            enrollments = courseIds.isEmpty() ? List.of() : enrollmentRepository.findByCourse_CourseIdIn(courseIds);
        }

        // Monthly breakdown
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Map<String, Long> monthlyMap = new TreeMap<>();
        for (Enrollments e : enrollments) {
            if (e.getEnrolledAt() != null) {
                String monthKey = sdf.format(e.getEnrolledAt());
                monthlyMap.put(monthKey, monthlyMap.getOrDefault(monthKey, 0L) + 1);
            }
        }
        List<MonthlyEnrollmentDTO> monthlyBreakdown = monthlyMap.entrySet().stream()
                .map(entry -> new MonthlyEnrollmentDTO(entry.getKey(), entry.getValue()))
                .toList();

        // Course breakdown
        List<CourseEnrollmentDTO> courseBreakdown = new ArrayList<>();
        for (Courses course : courses) {
            List<Enrollments> cEnrolls = enrollments.stream()
                    .filter(e -> e.getCourse() != null && e.getCourse().getCourseId().equals(course.getCourseId()))
                    .toList();

            long count = cEnrolls.size();
            long active = cEnrolls.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
            long completed = cEnrolls.stream().filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED).count();

            String instructorName = "Unknown";
            if (course.getInstructor() != null) {
                instructorName = course.getInstructor().getFullname() != null
                        ? course.getInstructor().getFullname()
                        : course.getInstructor().getUsername();
            }

            courseBreakdown.add(new CourseEnrollmentDTO(
                    course.getCourseId(),
                    course.getTitle(),
                    instructorName,
                    count,
                    active,
                    completed
            ));
        }

        return EnrollmentReportDTO.builder()
                .totalEnrollments(enrollments.size())
                .monthlyBreakdown(monthlyBreakdown)
                .courseBreakdown(courseBreakdown)
                .build();
    }

    @Transactional(readOnly = true)
    public ProgressReportDTO getProgressReport(String email, Long filterCourseId) {
        Users user = getUserByEmail(email);

        List<Courses> courses;
        if (user.getRole() == Roles.ADMIN) {
            if (filterCourseId != null) {
                courses = courseRepository.findById(filterCourseId).map(List::of).orElse(List.of());
            } else {
                courses = courseRepository.findAll();
            }
        } else {
            courses = courseRepository.findByInstructor_Email(email);
            if (filterCourseId != null) {
                courses = courses.stream()
                        .filter(c -> c.getCourseId().equals(filterCourseId))
                        .toList();
            }
        }

        List<CourseProgressReportDTO> courseProgressList = new ArrayList<>();
        long totalEnrolledAcrossCourses = 0;
        long totalCompletedAcrossCourses = 0;
        double sumQuizScores = 0.0;
        int coursesWithQuizData = 0;
        long totalPossibleSubmissions = 0;
        long totalActualSubmissions = 0;

        for (Courses course : courses) {
            List<Enrollments> cEnrolls = enrollmentRepository.findByCourse_CourseId(course.getCourseId());
            long enrolled = cEnrolls.size();
            long completed = cEnrolls.stream().filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED).count();
            Double completionRate = enrolled > 0
                    ? Math.round((completed * 100.0 / enrolled) * 100.0) / 100.0
                    : 0.0;

            // Quiz attempts for this course
            List<QuizAttempt> attempts = quizAttemptRepository.findByEnrollment_Course_CourseId(course.getCourseId());
            Double avgQuizScore = 0.0;
            if (!attempts.isEmpty()) {
                double sumPct = 0.0;
                int validAttempts = 0;
                for (QuizAttempt a : attempts) {
                    if (a.getTotalPoints() != null && a.getTotalPoints() > 0 && a.getEarnedPoints() != null) {
                        sumPct += (a.getEarnedPoints() / a.getTotalPoints()) * 100.0;
                        validAttempts++;
                    } else if (a.getTotalQuestions() != null && a.getTotalQuestions() > 0 && a.getCorrectAnswers() != null) {
                        sumPct += (a.getCorrectAnswers().doubleValue() / a.getTotalQuestions()) * 100.0;
                        validAttempts++;
                    }
                }
                if (validAttempts > 0) {
                    avgQuizScore = Math.round((sumPct / validAttempts) * 100.0) / 100.0;
                    sumQuizScores += avgQuizScore;
                    coursesWithQuizData++;
                }
            }

            // Assignments
            List<Assignment> assignments = assignmentRepository.findByCourse_CourseId(course.getCourseId());
            int totalAssignments = assignments.size();
            List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByAssignment_Course_CourseId(course.getCourseId());
            int totalSubmissions = submissions.size();
            int gradedSubmissions = (int) submissions.stream()
                    .filter(s -> s.getStatus() == AssignmentStatus.GRADED)
                    .count();

            totalEnrolledAcrossCourses += enrolled;
            totalCompletedAcrossCourses += completed;
            totalPossibleSubmissions += (enrolled * totalAssignments);
            totalActualSubmissions += totalSubmissions;

            courseProgressList.add(CourseProgressReportDTO.builder()
                    .courseId(course.getCourseId())
                    .courseTitle(course.getTitle())
                    .enrolledCount(enrolled)
                    .completedCount(completed)
                    .completionRate(completionRate)
                    .averageQuizScore(avgQuizScore)
                    .totalAssignments(totalAssignments)
                    .totalSubmissions(totalSubmissions)
                    .gradedSubmissions(gradedSubmissions)
                    .build());
        }

        Double overallCompletionRate = totalEnrolledAcrossCourses > 0
                ? Math.round((totalCompletedAcrossCourses * 100.0 / totalEnrolledAcrossCourses) * 100.0) / 100.0
                : 0.0;

        Double overallAvgQuizScore = coursesWithQuizData > 0
                ? Math.round((sumQuizScores / coursesWithQuizData) * 100.0) / 100.0
                : 0.0;

        Double assignmentSubmissionRate = totalPossibleSubmissions > 0
                ? Math.round((totalActualSubmissions * 100.0 / totalPossibleSubmissions) * 100.0) / 100.0
                : (totalActualSubmissions > 0 ? 100.0 : 0.0);

        return ProgressReportDTO.builder()
                .totalStudentsEnrolled(totalEnrolledAcrossCourses)
                .overallCompletionRate(overallCompletionRate)
                .overallAverageQuizScore(overallAvgQuizScore)
                .assignmentSubmissionRate(assignmentSubmissionRate)
                .courseProgressList(courseProgressList)
                .build();
    }

    @Transactional(readOnly = true)
    public QuizReportDTO getQuizReport(String email, Long filterQuizId) {
        Users user = getUserByEmail(email);

        List<Quizzes> quizzes;
        if (user.getRole() == Roles.ADMIN) {
            quizzes = filterQuizId != null
                    ? quizRepository.findById(filterQuizId).map(List::of).orElse(List.of())
                    : quizRepository.findAll();
        } else {
            List<Courses> courses = courseRepository.findByInstructor_Email(email);
            List<Long> courseIds = courses.stream().map(Courses::getCourseId).toList();
            quizzes = quizRepository.findAll().stream()
                    .filter(q -> {
                        if (q.getInstructor() != null && email.equalsIgnoreCase(q.getInstructor().getEmail())) {
                            return true;
                        }
                        if (q.getLesson() != null && q.getLesson().getSection() != null
                                && q.getLesson().getSection().getCourse() != null) {
                            return courseIds.contains(q.getLesson().getSection().getCourse().getCourseId());
                        }
                        return false;
                    })
                    .toList();

            if (filterQuizId != null) {
                quizzes = quizzes.stream().filter(q -> q.getQuizId().equals(filterQuizId)).toList();
            }
        }

        List<QuizSummaryReportDTO> summaries = new ArrayList<>();
        List<HardestQuestionDTO> hardestQuestions = new ArrayList<>();
        long totalAttemptsAll = 0;
        long totalPassedAll = 0;
        double sumScoreAll = 0.0;
        int validScoreAttempts = 0;

        for (Quizzes quiz : quizzes) {
            List<QuizAttempt> attempts = quizAttemptRepository.findByQuiz_QuizId(quiz.getQuizId());
            long count = attempts.size();
            totalAttemptsAll += count;

            long uniqueStudents = attempts.stream()
                    .filter(a -> a.getUser() != null)
                    .map(a -> a.getUser().getId())
                    .distinct()
                    .count();

            double sumScore = 0.0;
            double maxScore = 0.0;
            double minScore = count > 0 ? 100.0 : 0.0;
            long passedCount = 0;

            for (QuizAttempt a : attempts) {
                double pct = 0.0;
                if (a.getTotalPoints() != null && a.getTotalPoints() > 0 && a.getEarnedPoints() != null) {
                    pct = (a.getEarnedPoints() / a.getTotalPoints()) * 100.0;
                } else if (a.getTotalQuestions() != null && a.getTotalQuestions() > 0 && a.getCorrectAnswers() != null) {
                    pct = (a.getCorrectAnswers().doubleValue() / a.getTotalQuestions()) * 100.0;
                }

                sumScore += pct;
                sumScoreAll += pct;
                validScoreAttempts++;

                if (pct > maxScore) maxScore = pct;
                if (pct < minScore) minScore = pct;
                if (pct >= 70.0) {
                    passedCount++;
                    totalPassedAll++;
                }
            }

            Double avgScore = count > 0 ? Math.round((sumScore / count) * 100.0) / 100.0 : 0.0;
            Double passRate = count > 0 ? Math.round((passedCount * 100.0 / count) * 100.0) / 100.0 : 0.0;

            String courseTitle = "N/A";
            if (quiz.getLesson() != null && quiz.getLesson().getSection() != null
                    && quiz.getLesson().getSection().getCourse() != null) {
                courseTitle = quiz.getLesson().getSection().getCourse().getTitle();
            }

            summaries.add(QuizSummaryReportDTO.builder()
                    .quizId(quiz.getQuizId())
                    .quizTitle(quiz.getQuizTitle())
                    .courseTitle(courseTitle)
                    .totalAttempts(count)
                    .uniqueStudents(uniqueStudents)
                    .averageScore(avgScore)
                    .passRate(passRate)
                    .highestScore(Math.round(maxScore * 100.0) / 100.0)
                    .lowestScore(count > 0 ? Math.round(minScore * 100.0) / 100.0 : 0.0)
                    .build());

            // Questions analysis
            if (quiz.getQuestions() != null) {
                for (Questions q : quiz.getQuestions()) {
                    long incorrect = 0;
                    for (QuizAttempt a : attempts) {
                        if (a.getTotalQuestions() != null && a.getCorrectAnswers() != null) {
                            int missed = a.getTotalQuestions() - a.getCorrectAnswers();
                            if (missed > 0) incorrect++;
                        }
                    }
                    Double accuracyRate = count > 0
                            ? Math.round(((count - incorrect) * 100.0 / count) * 100.0) / 100.0
                            : 100.0;

                    hardestQuestions.add(HardestQuestionDTO.builder()
                            .questionId(q.getQuestionId())
                            .questionText(q.getQuestionText())
                            .totalAttempts(count)
                            .incorrectCount(incorrect)
                            .accuracyRate(accuracyRate)
                            .build());
                }
            }
        }

        hardestQuestions.sort(Comparator.comparing(HardestQuestionDTO::accuracyRate));
        if (hardestQuestions.size() > 5) {
            hardestQuestions = hardestQuestions.subList(0, 5);
        }

        Double overallPassRate = totalAttemptsAll > 0
                ? Math.round((totalPassedAll * 100.0 / totalAttemptsAll) * 100.0) / 100.0
                : 0.0;

        Double overallAvgScore = validScoreAttempts > 0
                ? Math.round((sumScoreAll / validScoreAttempts) * 100.0) / 100.0
                : 0.0;

        return QuizReportDTO.builder()
                .totalQuizzes(quizzes.size())
                .totalAttempts(totalAttemptsAll)
                .overallPassRate(overallPassRate)
                .overallAverageScore(overallAvgScore)
                .quizSummaries(summaries)
                .hardestQuestions(hardestQuestions)
                .build();
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
