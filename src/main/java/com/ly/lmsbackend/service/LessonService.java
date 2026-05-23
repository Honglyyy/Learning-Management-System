package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.*;
import com.ly.lmsbackend.mapper.LessonMapper;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.Sections;
import com.ly.lmsbackend.repository.LessonRepository;
import com.ly.lmsbackend.repository.QuizRepository;
import com.ly.lmsbackend.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LessonService {
    private final LessonMapper lessonMapper;
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final QuizRepository quizRepository;

    public LessonService(LessonMapper lessonMapper, LessonRepository lessonRepository, SectionRepository sectionRepository, QuizRepository quizRepository) {
        this.lessonRepository = lessonRepository;
        this.lessonMapper = lessonMapper;
        this.sectionRepository = sectionRepository;
        this.quizRepository = quizRepository;
    }

    public List<LessonResponseDTO> getLessons() {
        return lessonRepository.findAll()
                .stream()
                .map(lessonMapper::toDto)
                .toList();
    }


    public LessonResponseDTO addLesson(LessonCreateDTO dto) {
        Lessons lesson = new Lessons();

        Sections section = sectionRepository.findById(dto.sectionId()).orElse(null);

        lesson = lessonMapper.toEntity(dto, section);

        return lessonMapper.toDto(lessonRepository.save(lesson));
    }

    public LessonResponseDTO updateLesson(Long id, LessonCreateDTO dto) {
        Lessons existingLesson = lessonRepository.findById(id).orElse(null);

        Sections sectionId = sectionRepository.findById(dto.sectionId()).orElse(null);

        existingLesson.setTitle(dto.title());
        existingLesson.setVideoDir(dto.videoDir());
        existingLesson.setSection(sectionId);

        return lessonMapper.toDto(lessonRepository.save(existingLesson));
    }

    @Transactional
    public void deleteLesson(Long id) {

        Lessons lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (lesson.getQuiz() != null) {
            lesson.getQuiz().setLesson(null);
        }

        lessonRepository.delete(lesson);
    }

    public LessonQuizDTO getLesson(Long lessonId){
        Lessons lessons = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        List<QuizDetailDTO> quizzes = quizRepository.findByLesson_LessonId(lessonId)
                .stream()
                .map(quiz -> new QuizDetailDTO(
                        quiz.getQuizId(),
                        quiz.getQuizTitle(),
                        quiz.getTotalPoint(),
                        quiz.getQuestions().stream()
                                .map(question -> new QuestionDetailDTO(
                                        question.getQuestionId(),
                                        question.getQuestionText(),
                                        question.getPoint(),
                                        question.getAnswers().stream()
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
                lessons.getVideoDir(),
                quizzes
        );
    }
}
