package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.answerdtos.AnswerCreateDTO;
import com.ly.lmsbackend.dto.answerdtos.AnswerResponseDTO;
import com.ly.lmsbackend.mapper.AnswerMapper;
import com.ly.lmsbackend.model.Answers;
import com.ly.lmsbackend.model.Questions;
import com.ly.lmsbackend.repository.AnswerRepository;
import com.ly.lmsbackend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final AnswerMapper answerMapper;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository, AnswerMapper answerMapper) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.answerMapper = answerMapper;
    }

    public AnswerResponseDTO addAnswer(AnswerCreateDTO dto){
        Questions question = questionRepository.findById(dto.questionId())
                .orElseThrow(() -> new RuntimeException("Question id " + dto.questionId() + " is now found!!"));

        Answers answers = answerMapper.toEntity(dto, question);

        return answerMapper.toDto(answerRepository.save(answers));
    }

    public List<AnswerResponseDTO> getAnswers() {
        return answerRepository.findAll()
                .stream()
                .map(answerMapper::toDto)
                .toList();
    }

    public Answers updateAnswer(
            Long id,
            AnswerCreateDTO dto
    ){
        Answers existingAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer id " + id + " is now found!!"));

        existingAnswer.setAnswerText(dto.answerText());
        existingAnswer.setIsCorrect(dto.isCorrect());
        existingAnswer.setQuestion(questionRepository.findById(dto.questionId())
                .orElseThrow(() -> new RuntimeException("Question id " + dto.questionId() + " is now found!!")));

        return answerRepository.save(existingAnswer);
    }

    public void deleteAnswer(Long id){
        answerRepository.deleteById(id);
    }


}
