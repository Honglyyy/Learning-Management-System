package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.questiondtos.QuestionDetailDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizAttemptResponseDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizCreateDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizDetailDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizResponseDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizSubmitDTO;
import com.ly.lmsbackend.dto.answerdtos.AnswerDetailDTO;
import com.ly.lmsbackend.mapper.QuizAttemptMapper;
import com.ly.lmsbackend.mapper.QuizMapper;
import com.ly.lmsbackend.model.Answers;
import com.ly.lmsbackend.model.Courses;
import com.ly.lmsbackend.model.EnrollmentStatus;
import com.ly.lmsbackend.model.Enrollments;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.QuizAttempt;
import com.ly.lmsbackend.model.Questions;
import com.ly.lmsbackend.model.Quizzes;
import com.ly.lmsbackend.repository.EnrollmentRepository;
import com.ly.lmsbackend.repository.LessonRepository;
import com.ly.lmsbackend.repository.QuizAttemptRepository;
import com.ly.lmsbackend.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAttemptMapper quizAttemptMapper;
    private final StudentPointsService studentPointsService;
    private final ActivityLogService activityLogService;
    private final ProgressService progressService;

    public QuizService(
            QuizRepository quizRepository,
            QuizMapper quizMapper,
            LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository,
            QuizAttemptRepository quizAttemptRepository,
            QuizAttemptMapper quizAttemptMapper
    ) {
        this(quizRepository, quizMapper, lessonRepository, enrollmentRepository, quizAttemptRepository, quizAttemptMapper, null, null, null);
    }

    @Autowired
    public QuizService(
            QuizRepository quizRepository,
            QuizMapper quizMapper,
            LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository,
            QuizAttemptRepository quizAttemptRepository,
            QuizAttemptMapper quizAttemptMapper,
            StudentPointsService studentPointsService,
            ActivityLogService activityLogService,
            ProgressService progressService
    ) {
        this.quizRepository = quizRepository;
        this.quizMapper = quizMapper;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAttemptMapper = quizAttemptMapper;
        this.studentPointsService = studentPointsService;
        this.activityLogService = activityLogService;
        this.progressService = progressService;
    }

    public List<QuizResponseDTO> getQuizzes(){
        return quizRepository.findAll()
                .stream()
                .map(quizMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizDetailDTO getQuizByLesson(Long lessonId) {
        Quizzes quiz = quizRepository.findByLesson_LessonId(lessonId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Quiz for lesson id " + lessonId + " is not found"
                ));

        return new QuizDetailDTO(
                quiz.getQuizId(),
                quiz.getQuizTitle(),
                quiz.getTotalPoint(),
                quiz.getQuestions() == null ? List.of() : quiz.getQuestions()
                        .stream()
                        .map(question -> new QuestionDetailDTO(
                                question.getQuestionId(),
                                question.getQuestionText(),
                                question.getPoint(),
                                question.getAnswers() == null ? List.of() : question.getAnswers()
                                        .stream()
                                        .map(answer -> new AnswerDetailDTO(
                                                answer.getAnswerId(),
                                                answer.getAnswerText(),
                                                answer.getIsCorrect()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponseDTO getMyAttempt(Long quizId, String email) {
        return quizAttemptRepository.findByUser_EmailAndQuiz_QuizId(email, quizId)
                .map(quizAttemptMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz attempt not found"));
    }

    @Transactional
    public QuizAttemptResponseDTO submitQuiz(Long quizId, QuizSubmitDTO dto, String email) {
        Quizzes quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        Long courseId = quiz.getLesson().getSection().getCourse().getCourseId();
        Enrollments enrollment = enrollmentRepository.findByUser_EmailAndCourse_CourseId(email, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not enrolled in this course"));

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your enrollment is not active");
        }

        Set<Long> selectedAnswerIds = new HashSet<>(dto.answerIds() == null ? List.of() : dto.answerIds());
        List<Questions> questions = quiz.getQuestions() == null ? List.of() : quiz.getQuestions();
        double totalPoints = quiz.getTotalPoint() == null ? 0.0 : quiz.getTotalPoint();
        double defaultQuestionPoints = questions.isEmpty() ? 0.0 : totalPoints / questions.size();

        double earnedPoints = 0.0;
        int correctAnswers = 0;

        for (Questions question : questions) {
            Answers correctAnswer = question.getAnswers() == null ? null : question.getAnswers()
                    .stream()
                    .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                    .findFirst()
                    .orElse(null);

            if (correctAnswer != null && selectedAnswerIds.contains(correctAnswer.getAnswerId())) {
                earnedPoints += question.getPoint() == null ? defaultQuestionPoints : question.getPoint();
                correctAnswers++;
            }
        }

        QuizAttempt attempt = quizAttemptRepository
                .findByEnrollment_EnrollmentIdAndQuiz_QuizId(enrollment.getEnrollmentId(), quiz.getQuizId())
                .orElseGet(QuizAttempt::new);

        attempt.setEnrollment(enrollment);
        attempt.setUser(enrollment.getUser());
        attempt.setQuiz(quiz);
        attempt.setEarnedPoints(earnedPoints);
        attempt.setTotalPoints(totalPoints);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setTotalQuestions(questions.size());

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Sync and add quiz points with assignment points for student & course
        Courses course = quiz.getLesson() != null && quiz.getLesson().getSection() != null
                ? quiz.getLesson().getSection().getCourse()
                : null;
        if (studentPointsService != null) {
            if (course != null) {
                studentPointsService.recalculateCourseAndStudentPoints(enrollment.getUser(), course, enrollment);
            } else {
                studentPointsService.syncGlobalStudentPoints(enrollment.getUser());
            }
        }

        // Log activity
        if (activityLogService != null) {
            activityLogService.logActivity(
                    enrollment.getUser(),
                    "QUIZ_ATTEMPT",
                    "Submitted quiz: " + quiz.getQuizTitle() + " (" + correctAnswers + "/" + questions.size() + " correct, " + earnedPoints + " pts)"
            );
        }

        if (course != null && progressService != null) {
            progressService.checkCourseCompletion(enrollment.getUser(), course);
        }

        return quizAttemptMapper.toDto(savedAttempt);
    }

    public QuizResponseDTO addQuiz(QuizCreateDTO dto){
        Quizzes quiz = new Quizzes();

        Lessons lesson = lessonRepository.findById(dto.lessonId())
                .orElseThrow(() -> new RuntimeException("Lesson id " + dto.lessonId() + " is not found"));

        quiz = quizMapper.toEntity(dto, lesson);

        return quizMapper.toDto(quizRepository.save(quiz));
    }

    public QuizResponseDTO updateQuiz(Long id, QuizCreateDTO dto){
       Quizzes existingQuiz = quizRepository.findById(id).orElseThrow(() -> new RuntimeException("Quiz not found!!"));

       Lessons lessonId = lessonRepository.findById(dto.lessonId()).orElseThrow(() -> new RuntimeException("Lesson not found!!"));

       existingQuiz.setQuizTitle(dto.title());
       existingQuiz.setTotalPoint(dto.totalPoints());
       existingQuiz.setLesson(lessonId);

       return quizMapper.toDto(quizRepository.save(existingQuiz));
    }

    @Transactional
    public void deleteQuiz(Long id){
        if (!quizRepository.existsById(id)) {
            throw new RuntimeException("Quiz not found");
        }

        quizAttemptRepository.deleteAllByQuizId(id);
        quizRepository.deleteById(id);
    }
}
