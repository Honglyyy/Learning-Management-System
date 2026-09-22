package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.lessondtos.LessonNavigationDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonProgressDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonStatusDTO;
import com.ly.lmsbackend.model.*;
import com.ly.lmsbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final ProgressService progressService;

    public LessonProgressService(
            LessonProgressRepository lessonProgressRepository,
            LessonRepository lessonRepository,
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository
    ) {
        this(lessonProgressRepository, lessonRepository, studentRepository, enrollmentRepository, courseRepository, userRepository, null, null);
    }

    @Autowired
    public LessonProgressService(
            LessonProgressRepository lessonProgressRepository,
            LessonRepository lessonRepository,
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            ActivityLogService activityLogService,
            ProgressService progressService
    ) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.lessonRepository = lessonRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
        this.progressService = progressService;
    }

    @Transactional
    public LessonProgressDTO markComplete(Long lessonId, String userEmail) {
        Lessons lesson = getLesson(lessonId);
        Students student = getStudentByEmail(userEmail);
        verifyStudentEnrolledInLesson(student, lesson);

        LessonProgress progress = lessonProgressRepository
                .findByStudentAndLesson(student, lesson)
                .orElseGet(() -> LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .lastPlaybackPositionSeconds(0.0)
                        .build());

        progress.setIsCompleted(true);
        progress.setCompletedAt(new Timestamp(System.currentTimeMillis()));

        LessonProgress saved = lessonProgressRepository.save(progress);

        Courses course = lesson.getSection() != null ? lesson.getSection().getCourse() : null;
        if (student.getUser() != null) {
            String courseTitle = course != null ? " in " + course.getTitle() : "";
            if (activityLogService != null) {
                activityLogService.logActivity(
                        student.getUser(),
                        "LESSON_COMPLETED",
                        "Completed lesson: " + lesson.getTitle() + courseTitle
                );
            }

            if (course != null && progressService != null) {
                progressService.checkCourseCompletion(student.getUser(), course);
            }
        }

        return toDTO(saved);
    }

    @Transactional
    public LessonProgressDTO savePlaybackPosition(Long lessonId, Double seconds, String userEmail) {
        Lessons lesson = getLesson(lessonId);
        Students student = getStudentByEmail(userEmail);
        verifyStudentEnrolledInLesson(student, lesson);

        LessonProgress progress = lessonProgressRepository
                .findByStudentAndLesson(student, lesson)
                .orElseGet(() -> LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .isCompleted(false)
                        .build());

        double position = (seconds != null && seconds >= 0) ? seconds : 0.0;
        progress.setLastPlaybackPositionSeconds(position);

        LessonProgress saved = lessonProgressRepository.save(progress);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public LessonProgressDTO getProgress(Long lessonId, String userEmail) {
        Lessons lesson = getLesson(lessonId);
        Students student = getStudentByEmail(userEmail);
        verifyStudentEnrolledInLesson(student, lesson);

        return lessonProgressRepository
                .findByStudentAndLesson(student, lesson)
                .map(this::toDTO)
                .orElse(new LessonProgressDTO(lessonId, false, 0.0, null));
    }

    @Transactional(readOnly = true)
    public LessonNavigationDTO getNavigation(Long lessonId, String userEmail) {
        Lessons currentLesson = getLesson(lessonId);
        Long courseId = getCourseIdForLesson(currentLesson);

        Users user = (userEmail != null && !userEmail.isBlank())
                ? userRepository.findByEmail(userEmail).orElse(null)
                : null;

        // If lesson is not free, verify user authentication and access
        if (!Boolean.TRUE.equals(currentLesson.getIsFree())) {
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to access this lesson");
            }
            if (user.getRole() == Roles.STUDENT) {
                Students student = getStudentByEmail(userEmail);
                verifyStudentEnrolledInCourse(student, courseId);
            } else if (user.getRole() == Roles.INSTRUCTOR) {
                Courses course = courseRepository.findById(courseId).orElse(null);
                if (course != null && course.getInstructor() != null && !user.getEmail().equals(course.getInstructor().getEmail())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to instructor of another course");
                }
            }
        }

        boolean hasCourseAccess = false;
        if (user != null) {
            if (user.getRole() == Roles.ADMIN) {
                hasCourseAccess = true;
            } else if (user.getRole() == Roles.INSTRUCTOR) {
                Courses course = courseRepository.findById(courseId).orElse(null);
                if (course != null && course.getInstructor() != null && user.getEmail().equals(course.getInstructor().getEmail())) {
                    hasCourseAccess = true;
                }
            } else if (user.getRole() == Roles.STUDENT) {
                hasCourseAccess = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                        .map(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                        .orElse(false);
            }
        }

        List<Lessons> courseLessons = lessonRepository
                .findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(courseId);

        int currentIndex = -1;
        for (int i = 0; i < courseLessons.size(); i++) {
            if (courseLessons.get(i).getLessonId().equals(lessonId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            return new LessonNavigationDTO(lessonId, null, null, false, false);
        }

        Lessons prevLesson = (currentIndex > 0) ? courseLessons.get(currentIndex - 1) : null;
        Lessons nextLesson = (currentIndex < courseLessons.size() - 1) ? courseLessons.get(currentIndex + 1) : null;

        Long previousLessonId = null;
        if (prevLesson != null && (hasCourseAccess || Boolean.TRUE.equals(prevLesson.getIsFree()))) {
            previousLessonId = prevLesson.getLessonId();
        }

        Long nextLessonId = null;
        if (nextLesson != null && (hasCourseAccess || Boolean.TRUE.equals(nextLesson.getIsFree()))) {
            nextLessonId = nextLesson.getLessonId();
        }

        return new LessonNavigationDTO(
                lessonId,
                previousLessonId,
                nextLessonId,
                previousLessonId != null,
                nextLessonId != null
        );
    }

    @Transactional(readOnly = true)
    public List<LessonStatusDTO> getLessonsStatus(Long courseId, String userEmail) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }

        List<Lessons> courseLessons = lessonRepository
                .findBySection_Course_CourseIdOrderBySection_SectionIdAscOrderIndexAsc(courseId);

        if (courseLessons.isEmpty()) {
            return List.of();
        }

        if (userEmail == null) {
            return courseLessons.stream()
                    .map(lesson -> new LessonStatusDTO(
                            lesson.getLessonId(),
                            lesson.getTitle(),
                            lesson.getOrderIndex(),
                            lesson.getDuration(),
                            Boolean.TRUE.equals(lesson.getIsFree()) ? "INCOMPLETE" : "LOCKED",
                            Boolean.TRUE.equals(lesson.getIsFree())
                    ))
                    .toList();
        }

        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // For Instructor / Admin, all lessons are accessible (unlocked)
        if (user.getRole() == Roles.ADMIN || (user.getRole() == Roles.INSTRUCTOR)) {
            return courseLessons.stream()
                    .map(lesson -> new LessonStatusDTO(
                            lesson.getLessonId(),
                            lesson.getTitle(),
                            lesson.getOrderIndex(),
                            lesson.getDuration(),
                            "INCOMPLETE",
                            Boolean.TRUE.equals(lesson.getIsFree())
                    ))
                    .toList();
        }

        // For Student, verify enrollment and compute COMPLETED / INCOMPLETE / LOCKED
        Students student = getStudentByEmail(userEmail);
        boolean isEnrolled = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                .map(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .orElse(false);

        if (!isEnrolled) {
            return courseLessons.stream()
                    .map(lesson -> new LessonStatusDTO(
                            lesson.getLessonId(),
                            lesson.getTitle(),
                            lesson.getOrderIndex(),
                            lesson.getDuration(),
                            Boolean.TRUE.equals(lesson.getIsFree()) ? "INCOMPLETE" : "LOCKED",
                            Boolean.TRUE.equals(lesson.getIsFree())
                    ))
                    .toList();
        }

        Map<Long, LessonProgress> progressMap = lessonProgressRepository
                .findByStudentAndLessonIn(student, courseLessons)
                .stream()
                .collect(Collectors.toMap(lp -> lp.getLesson().getLessonId(), lp -> lp));

        List<LessonStatusDTO> statusList = new ArrayList<>();
        boolean previousCompleted = true; // Lesson 0 is always unlocked

        for (int i = 0; i < courseLessons.size(); i++) {
            Lessons lesson = courseLessons.get(i);
            LessonProgress progress = progressMap.get(lesson.getLessonId());
            boolean isCompleted = progress != null && Boolean.TRUE.equals(progress.getIsCompleted());

            String status;
            if (isCompleted) {
                status = "COMPLETED";
                previousCompleted = true;
            } else if (i == 0 || previousCompleted || Boolean.TRUE.equals(lesson.getIsFree())) {
                status = "INCOMPLETE";
                previousCompleted = false;
            } else {
                status = "LOCKED";
                previousCompleted = false;
            }

            statusList.add(new LessonStatusDTO(
                    lesson.getLessonId(),
                    lesson.getTitle(),
                    lesson.getOrderIndex(),
                    lesson.getDuration(),
                    status,
                    Boolean.TRUE.equals(lesson.getIsFree())
            ));
        }

        return statusList;
    }

    private Lessons getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found with id: " + lessonId));
    }

    private Students getStudentByEmail(String email) {
        return studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found for user: " + email));
    }

    private Long getCourseIdForLesson(Lessons lesson) {
        if (lesson.getSection() == null || lesson.getSection().getCourse() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson is not assigned to a valid section/course");
        }
        return lesson.getSection().getCourse().getCourseId();
    }

    private void verifyStudentEnrolledInLesson(Students student, Lessons lesson) {
        if (Boolean.TRUE.equals(lesson.getIsFree())) {
            return;
        }
        Long courseId = getCourseIdForLesson(lesson);
        verifyStudentEnrolledInCourse(student, courseId);
    }

    private void verifyStudentEnrolledInCourse(Students student, Long courseId) {
        Users user = student.getUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account not linked to student");
        }

        boolean isEnrolled = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                .map(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .orElse(false);

        if (!isEnrolled) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: student is not actively enrolled in this course");
        }
    }

    private LessonProgressDTO toDTO(LessonProgress progress) {
        return new LessonProgressDTO(
                progress.getLesson().getLessonId(),
                progress.getIsCompleted(),
                progress.getLastPlaybackPositionSeconds(),
                progress.getCompletedAt()
        );
    }
}
