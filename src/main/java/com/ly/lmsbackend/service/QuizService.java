package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.QuizCreateDTO;
import com.ly.lmsbackend.dto.QuizResponseDTO;
import com.ly.lmsbackend.mapper.QuizMapper;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.Quizzes;
import com.ly.lmsbackend.repository.LessonRepository;
import com.ly.lmsbackend.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private final LessonRepository lessonRepository;

    public QuizService(QuizRepository quizRepository, QuizMapper quizMapper, LessonRepository lessonRepository) {
        this.quizRepository = quizRepository;
        this.quizMapper = quizMapper;
        this.lessonRepository = lessonRepository;
    }

    public List<QuizResponseDTO> getQuizzes(){
        return quizRepository.findAll()
                .stream()
                .map(quizMapper::toDto)
                .toList();
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

    public void deleteQuiz(Long id){
        quizRepository.deleteById(id);
    }
}
