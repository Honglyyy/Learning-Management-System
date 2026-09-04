package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.questiondtos.QuestionCreateDTO;
import com.ly.lmsbackend.dto.questiondtos.QuestionResponseDTO;
import com.ly.lmsbackend.mapper.QuestionMapper;
import com.ly.lmsbackend.model.Questions;
import com.ly.lmsbackend.model.Quizzes;
import com.ly.lmsbackend.repository.QuestionRepository;
import com.ly.lmsbackend.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionMapper questionMapper, QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionMapper = questionMapper;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public List<QuestionResponseDTO> getQuestions(){
        return questionRepository.findAll()
                .stream()
                .map(questionMapper::toDto)
                .toList();
    }

    public QuestionResponseDTO addQuesion(QuestionCreateDTO dto){
        Questions question = new Questions();

        Quizzes quiz = quizRepository.findById(dto.quizId()).orElseThrow(()->new RuntimeException("Quiz not found"));


        question = questionMapper.toEntity(dto, quiz);

        return questionMapper.toDto(questionRepository.save(question));
    }

    public QuestionResponseDTO updateQuestion(Long id, QuestionCreateDTO dto){
        Questions existingQuestion = questionRepository.findById(id).orElseThrow(()->new RuntimeException("Questions not found"));

        Quizzes quizId = quizRepository.findById(dto.quizId()).orElseThrow(()-> new RuntimeException("Quiz not found"));

        existingQuestion.setQuestionText(dto.questionText());
        existingQuestion.setPoint(dto.point());
        existingQuestion.setQuiz(quizId);

        return questionMapper.toDto(questionRepository.save(existingQuestion));
    }

    public void deleteQuestion(Long id){
        questionRepository.deleteById(id);
    }
}
