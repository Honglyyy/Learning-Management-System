package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.answerdtos.AnswerDetailDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonCreateDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonQuizDTO;
import com.ly.lmsbackend.dto.lessondtos.LessonResponseDTO;
import com.ly.lmsbackend.dto.questiondtos.QuestionDetailDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizDetailDTO;
import com.ly.lmsbackend.mapper.LessonMapper;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.Sections;
import com.ly.lmsbackend.repository.LessonProgressRepository;
import com.ly.lmsbackend.repository.LessonRepository;
import com.ly.lmsbackend.repository.QuizAttemptRepository;
import com.ly.lmsbackend.repository.QuizRepository;
import com.ly.lmsbackend.repository.SectionRepository;
import com.ly.lmsbackend.repository.CourseRepository;
import com.ly.lmsbackend.repository.UserRepository;
import com.ly.lmsbackend.repository.EnrollmentRepository;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.model.EnrollmentStatus;
import com.ly.lmsbackend.model.Enrollments;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LessonService {
    private final LessonMapper lessonMapper;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileUploadService fileUploadService;

    public LessonService(
            LessonMapper lessonMapper,
            LessonRepository lessonRepository,
            SectionRepository sectionRepository,
            CourseRepository courseRepository,
            QuizRepository quizRepository,
            QuizAttemptRepository quizAttemptRepository,
            LessonProgressRepository lessonProgressRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository
    ) {
        this(lessonMapper, lessonRepository, sectionRepository, courseRepository, quizRepository,
                quizAttemptRepository, lessonProgressRepository, userRepository, enrollmentRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public LessonService(
            LessonMapper lessonMapper,
            LessonRepository lessonRepository,
            SectionRepository sectionRepository,
            CourseRepository courseRepository,
            QuizRepository quizRepository,
            QuizAttemptRepository quizAttemptRepository,
            LessonProgressRepository lessonProgressRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) FileUploadService fileUploadService
    ) {
        this.lessonRepository = lessonRepository;
        this.lessonMapper = lessonMapper;
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fileUploadService = fileUploadService;
    }

    public List<LessonResponseDTO> getLessons() {
        return lessonRepository.findAll()
                .stream()
                .map(lessonMapper::toDto)
                .toList();
    }


    public LessonResponseDTO addLesson(LessonCreateDTO dto) {
        Sections section = sectionRepository.findById(dto.sectionId()).orElse(null);
        Lessons lesson = lessonMapper.toEntity(dto, section);
        organizeLessonVideo(lesson);
        return lessonMapper.toDto(lessonRepository.save(lesson));
    }

    public LessonResponseDTO updateLesson(Long id, LessonCreateDTO dto) {
        Lessons existingLesson = lessonRepository.findById(id).orElse(null);

        Sections sectionId = sectionRepository.findById(dto.sectionId()).orElse(null);

        existingLesson.setTitle(dto.title());
        existingLesson.setVideoUrl(dto.videoUrl());
        existingLesson.setVideoPublicId(dto.videoPublicId());
        if (dto.description() != null) existingLesson.setDescription(dto.description());
        if (dto.textContent() != null) existingLesson.setTextContent(dto.textContent());
        if (dto.orderIndex() != null) existingLesson.setOrderIndex(dto.orderIndex());
        if (dto.duration() != null) existingLesson.setDuration(dto.duration());
        if (dto.isFree() != null) existingLesson.setIsFree(dto.isFree());
        existingLesson.setSection(sectionId);

        organizeLessonVideo(existingLesson);
        return lessonMapper.toDto(lessonRepository.save(existingLesson));
    }

    @Transactional
    public void deleteLesson(Long id) {

        Lessons lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (fileUploadService != null && lesson.getVideoPublicId() != null && !lesson.getVideoPublicId().isBlank()) {
            fileUploadService.deleteAsset(lesson.getVideoPublicId());
        }

        lessonProgressRepository.deleteAllByLesson_LessonId(id);
        quizAttemptRepository.deleteAllByLessonId(id);
        lessonRepository.delete(lesson);
    }

    public LessonQuizDTO getLesson(Long lessonId) {
        return getLesson(lessonId, null);
    }

    public LessonQuizDTO getLesson(Long lessonId, String userEmail) {
        Lessons lessons = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));

        if (!Boolean.TRUE.equals(lessons.getIsFree())) {
            if (userEmail == null || userEmail.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to access this lesson");
            }
            Users user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            Long courseId = (lessons.getSection() != null && lessons.getSection().getCourse() != null)
                    ? lessons.getSection().getCourse().getCourseId()
                    : null;

            if (user.getRole() == Roles.STUDENT || user.getRole() == Roles.USER) {
                if (courseId == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course not found for this lesson");
                }
                Enrollments enrollment = enrollmentRepository.findByUser_IdAndCourse_CourseId(user.getId(), courseId)
                        .orElse(null);
                if (enrollment == null || enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Enrollment required to access this lesson");
                }
                if (enrollment.isExpired()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course access has expired. Please renew your access.");
                }
            } else if (user.getRole() == Roles.INSTRUCTOR) {
                if (courseId != null) {
                    Courses course = courseRepository.findById(courseId).orElse(null);
                    if (course != null && course.getInstructor() != null && !user.getEmail().equals(course.getInstructor().getEmail())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to instructor of another course");
                    }
                }
            }
        }

        List<QuizDetailDTO> quizzes = quizRepository.findByLesson_LessonId(lessonId)
                .stream()
                .map(quiz -> new QuizDetailDTO(
                        quiz.getQuizId(),
                        quiz.getQuizTitle(),
                        quiz.getTotalPoint(),
                        quiz.getQuestions() == null ? List.of() : quiz.getQuestions().stream()
                                .map(question -> new QuestionDetailDTO(
                                        question.getQuestionId(),
                                        question.getQuestionText(),
                                        question.getPoint(),
                                        question.getAnswers() == null ? List.of() : question.getAnswers().stream()
                                                .map(answer -> new AnswerDetailDTO(
                                                        answer.getAnswerId(),
                                                        answer.getAnswerText(),
                                                        answer.getIsCorrect()
                                                )).toList()
                                )).toList()
                        )
                ).toList();

        return new LessonQuizDTO(
                lessons.getLessonId(),
                lessons.getTitle(),
                lessons.getVideoUrl(),
                lessons.getVideoPublicId(),
                quizzes
        );
    }

    public List<LessonResponseDTO> getLessonByInstructor(String instructorEmail) {
        return lessonRepository.findByInstructor_Email(instructorEmail).stream().map(lessonMapper::toDto).toList();
    }

    public List<LessonResponseDTO> getLessonByInstructorAndSection(String instructorEmail, Long sectionId) {
        Sections section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        if (!section.getCourse().getInstructor().getEmail().equals(instructorEmail)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return lessonRepository.findByInstructor_EmailAndSection_SectionId(instructorEmail, sectionId)
                .stream()
                .map(lessonMapper::toDto)
                .toList();
    }

    public List<LessonResponseDTO> getLessonByInstructorAndCourse(String instructorEmail, Long courseId) {
        Courses course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstructor().getEmail().equals(instructorEmail)) {
            throw new AccessDeniedException("Unauthorized");
        }

        return lessonRepository.findByInstructor_EmailAndSection_Course_CourseId(instructorEmail, courseId)
                .stream()
                .map(lessonMapper::toDto)
                .toList();
    }

    public LessonResponseDTO addLessonByInstructor(
            LessonCreateDTO dto,
            String instructorEmail
    ) {

        Sections section = sectionRepository
                .findById(dto.sectionId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Section not found"
                        )
                );

        if (!section.getInstructor()
                .getEmail()
                .equals(instructorEmail)) {

            throw new AccessDeniedException(
                    "Unauthorized"
            );
        }

        Lessons lessons = lessonMapper
                .toEntity(dto, section);

        lessons.setInstructor(
                section.getInstructor()
        );

        organizeLessonVideo(lessons);

        return lessonMapper.toDto(
                lessonRepository.save(lessons)
        );
    }

    @Transactional
    public void deleteMyLesson(
            Long id,
            String instructorEmail
    ) {

        Lessons lesson = lessonRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lesson not found"
                        )
                );

        if (!lesson.getInstructor()
                .getEmail()
                .equals(instructorEmail)) {

            throw new AccessDeniedException(
                    "Unauthorized"
            );
        }

        if (fileUploadService != null && lesson.getVideoPublicId() != null && !lesson.getVideoPublicId().isBlank()) {
            fileUploadService.deleteAsset(lesson.getVideoPublicId());
        }

        lessonProgressRepository.deleteAllByLesson_LessonId(id);
        quizAttemptRepository.deleteAllByLessonId(id);
        lessonRepository.delete(lesson);
    }


    public LessonResponseDTO updateMyLesson(
            Long id,
            LessonCreateDTO dto,
            String instructorEmail
    ) {

        Lessons lesson = lessonRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Lesson not found")
                );

        // SECURITY CHECK
        if (!lesson.getInstructor()
                .getEmail()
                .equals(instructorEmail)) {

            throw new AccessDeniedException("Unauthorized");
        }

        // UPDATE FIELDS
        lesson.setTitle(dto.title());
        lesson.setVideoUrl(dto.videoUrl());
        lesson.setVideoPublicId(dto.videoPublicId());
        if (dto.description() != null) lesson.setDescription(dto.description());
        if (dto.textContent() != null) lesson.setTextContent(dto.textContent());
        if (dto.orderIndex() != null) lesson.setOrderIndex(dto.orderIndex());
        if (dto.duration() != null) lesson.setDuration(dto.duration());
        if (dto.isFree() != null) lesson.setIsFree(dto.isFree());

        organizeLessonVideo(lesson);

        Lessons updated = lessonRepository.save(lesson);

        return lessonMapper.toDto(updated);
    }

    private void organizeLessonVideo(Lessons lesson) {
        if (fileUploadService == null || lesson == null || lesson.getVideoPublicId() == null || lesson.getVideoPublicId().isBlank()) {
            return;
        }
        String instructorUsername = null;
        if (lesson.getInstructor() != null) {
            instructorUsername = lesson.getInstructor().getUsername();
        } else if (lesson.getSection() != null && lesson.getSection().getInstructor() != null) {
            instructorUsername = lesson.getSection().getInstructor().getUsername();
        } else if (lesson.getSection() != null && lesson.getSection().getCourse() != null && lesson.getSection().getCourse().getInstructor() != null) {
            instructorUsername = lesson.getSection().getCourse().getInstructor().getUsername();
        }
        String courseTitle = (lesson.getSection() != null && lesson.getSection().getCourse() != null)
                ? lesson.getSection().getCourse().getTitle()
                : null;
        var organized = fileUploadService.organizeLessonVideo(lesson.getVideoPublicId(), instructorUsername, courseTitle, lesson.getTitle());
        if (organized != null) {
            lesson.setVideoPublicId(organized.publicId());
            lesson.setVideoUrl(organized.url());
        }
    }
}
