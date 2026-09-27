package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.progressdtos.ContinueLearningDTO;
import com.ly.lmsbackend.dto.progressdtos.CourseProgressDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.*;

@Service
public class ProgressService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentPointsService studentPointsService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public ProgressService(
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            LessonRepository lessonRepository,
            LessonProgressRepository lessonProgressRepository,
            QuizRepository quizRepository,
            QuizAttemptRepository quizAttemptRepository,
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository submissionRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            StudentPointsService studentPointsService,
            NotificationService notificationService,
            ActivityLogService activityLogService
    ) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studentPointsService = studentPointsService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public CourseProgressDTO getCourseProgress(Long courseId, String email) {
        Users user = getUserByEmail(email);
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Enrollments enrollment = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not enrolled in this course"));

        if (enrollment.isExpired()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course access has expired. Please renew your access.");
        }

        // Recalculate and update points
        studentPointsService.recalculateCourseAndStudentPoints(user, course, enrollment);

        // Fetch all course lessons
        List<Lessons> lessons = lessonRepository
                .findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(courseId);
        int totalLessons = lessons.size();

        Students student = studentRepository.findByUser_Id(user.getId()).orElse(null);

        int completedLessons = 0;
        if (student != null && !lessons.isEmpty()) {
            completedLessons = (int) lessonProgressRepository.findByStudentAndLessonIn(student, lessons)
                    .stream()
                    .filter(lp -> Boolean.TRUE.equals(lp.getIsCompleted()))
                    .count();
        }

        // Fetch course quizzes
        List<Quizzes> quizzes = quizRepository.findAll().stream()
                .filter(q -> q.getLesson() != null
                        && q.getLesson().getSection() != null
                        && q.getLesson().getSection().getCourse() != null
                        && q.getLesson().getSection().getCourse().getCourseId().equals(courseId))
                .toList();
        int totalQuizzes = quizzes.size();

        int completedQuizzes = 0;
        if (!quizzes.isEmpty()) {
            Set<Long> quizIds = new HashSet<>();
            for (Quizzes q : quizzes) {
                quizIds.add(q.getQuizId());
            }
            completedQuizzes = (int) quizAttemptRepository.findAll().stream()
                    .filter(qa -> qa.getUser() != null
                            && qa.getUser().getId().equals(user.getId())
                            && qa.getQuiz() != null
                            && quizIds.contains(qa.getQuiz().getQuizId()))
                    .map(qa -> qa.getQuiz().getQuizId())
                    .distinct()
                    .count();
        }

        // Fetch course assignments
        List<Assignment> assignments = assignmentRepository.findByCourse_CourseIdOrderByCreatedAtDesc(courseId);
        int totalAssignments = assignments.size();

        int completedAssignments = 0;
        if (!assignments.isEmpty()) {
            Set<Long> assignmentIds = new HashSet<>();
            for (Assignment a : assignments) {
                assignmentIds.add(a.getAssignmentId());
            }
            completedAssignments = (int) submissionRepository.findByStudent_Id(user.getId()).stream()
                    .filter(sub -> sub.getAssignment() != null && assignmentIds.contains(sub.getAssignment().getAssignmentId()))
                    .filter(sub -> sub.getStatus() == AssignmentStatus.SUBMITTED
                            || sub.getStatus() == AssignmentStatus.LATE
                            || sub.getStatus() == AssignmentStatus.REVIEWED
                            || sub.getStatus() == AssignmentStatus.GRADED)
                    .map(sub -> sub.getAssignment().getAssignmentId())
                    .distinct()
                    .count();
        }

        // Weighted progress calculation
        double lessonWeight = totalLessons > 0 ? 0.5 : 0.0;
        double quizWeight = totalQuizzes > 0 ? 0.25 : 0.0;
        double assignmentWeight = totalAssignments > 0 ? 0.25 : 0.0;
        double sumWeights = lessonWeight + quizWeight + assignmentWeight;

        double progressPct = 0.0;
        if (sumWeights > 0) {
            double lessonPart = totalLessons > 0 ? ((double) completedLessons / totalLessons) * lessonWeight : 0.0;
            double quizPart = totalQuizzes > 0 ? ((double) completedQuizzes / totalQuizzes) * quizWeight : 0.0;
            double assignmentPart = totalAssignments > 0 ? ((double) completedAssignments / totalAssignments) * assignmentWeight : 0.0;
            progressPct = ((lessonPart + quizPart + assignmentPart) / sumWeights) * 100.0;
        } else {
            progressPct = 100.0;
        }

        progressPct = Math.min(100.0, Math.round(progressPct * 10.0) / 10.0);

        boolean isCompleted = progressPct >= 100.0;
        if (isCompleted && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollment);

            activityLogService.logActivity(user, "COURSE_COMPLETED", "Completed course: " + course.getTitle());
            notificationService.sendNotification(
                    user,
                    "Course Completed!",
                    "Congratulations! You completed " + course.getTitle() + ". You can now claim your certificate.",
                    "COURSE",
                    "/api/certificates/claim/" + courseId
            );
        }

        return CourseProgressDTO.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .totalQuizzes(totalQuizzes)
                .completedQuizzes(completedQuizzes)
                .totalAssignments(totalAssignments)
                .completedAssignments(completedAssignments)
                .progressPercentage(progressPct)
                .earnedPoints(enrollment.getEarnedPoints() != null ? enrollment.getEarnedPoints() : 0.0)
                .totalPoints(enrollment.getTotalPoints() != null ? enrollment.getTotalPoints() : 0.0)
                .status(enrollment.getStatus())
                .isCompleted(isCompleted)
                .build();
    }

    @Transactional
    public ContinueLearningDTO getContinueLearning(String email) {
        Users user = getUserByEmail(email);

        List<Enrollments> enrollments = enrollmentRepository.findByUser_Email(email);
        if (enrollments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No enrolled courses found for student");
        }

        // Find most recently updated active/completed non-expired enrollment
        Enrollments currentEnrollment = enrollments.stream()
                .filter(e -> (e.getStatus() == EnrollmentStatus.ACTIVE || e.getStatus() == EnrollmentStatus.COMPLETED) && !e.isExpired())
                .max(Comparator.comparing(e -> e.getUpdatedAt() != null ? e.getUpdatedAt() : e.getEnrolledAt()))
                .orElse(null);

        if (currentEnrollment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active enrolled courses found for student");
        }

        Courses course = currentEnrollment.getCourse();
        List<Lessons> lessons = lessonRepository
                .findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(course.getCourseId());

        Students student = studentRepository.findByUser_Id(user.getId()).orElse(null);

        Lessons nextLesson = null;
        Double lastPlaybackPosition = 0.0;

        if (student != null && !lessons.isEmpty()) {
            Map<Long, LessonProgress> progressMap = new HashMap<>();
            for (LessonProgress lp : lessonProgressRepository.findByStudentAndLessonIn(student, lessons)) {
                progressMap.put(lp.getLesson().getLessonId(), lp);
            }

            for (Lessons l : lessons) {
                LessonProgress lp = progressMap.get(l.getLessonId());
                if (lp == null || !Boolean.TRUE.equals(lp.getIsCompleted())) {
                    nextLesson = l;
                    if (lp != null && lp.getLastPlaybackPositionSeconds() != null) {
                        lastPlaybackPosition = lp.getLastPlaybackPositionSeconds();
                    }
                    break;
                }
            }

            // If all are completed, default to the first or last lesson
            if (nextLesson == null) {
                nextLesson = lessons.get(lessons.size() - 1);
            }
        } else if (!lessons.isEmpty()) {
            nextLesson = lessons.get(0);
        }

        CourseProgressDTO progressDTO = getCourseProgress(course.getCourseId(), email);

        return ContinueLearningDTO.builder()
                .courseId(course.getCourseId())
                .courseTitle(course.getTitle())
                .nextLessonId(nextLesson != null ? nextLesson.getLessonId() : null)
                .nextLessonTitle(nextLesson != null ? nextLesson.getTitle() : null)
                .nextLessonOrderIndex(nextLesson != null ? nextLesson.getOrderIndex() : null)
                .progressPercentage(progressDTO.progressPercentage())
                .lastPlaybackPositionSeconds(lastPlaybackPosition)
                .lastActivityAt(currentEnrollment.getUpdatedAt() != null ? currentEnrollment.getUpdatedAt() : currentEnrollment.getEnrolledAt())
                .build();
    }

    @Transactional
    public void checkCourseCompletion(Users user, Courses course) {
        if (user == null || course == null) {
            return;
        }
        try {
            getCourseProgress(course.getCourseId(), user.getEmail());
        } catch (Exception ignored) {
        }
    }

    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
