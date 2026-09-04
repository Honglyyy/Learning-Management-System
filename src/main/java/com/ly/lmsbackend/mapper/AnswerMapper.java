package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.answerdtos.AnswerCreateDTO;
import com.ly.lmsbackend.dto.answerdtos.AnswerResponseDTO;
import com.ly.lmsbackend.model.Answers;
import com.ly.lmsbackend.model.Questions;
import org.springframework.stereotype.Component;

@Component
public class AnswerMapper {
    public AnswerResponseDTO toDto(Answers answer){
        return new AnswerResponseDTO(
                answer.getAnswerId(),
                answer.getAnswerText(),
                answer.getIsCorrect(),
                answer.getQuestion().getQuestionId()
        );
    }

    public Answers toEntity(
            AnswerCreateDTO dto,
            Questions question
    ){
        Answers answers = new Answers();

        answers.setAnswerText(dto.answerText());
        answers.setIsCorrect(dto.isCorrect());
        answers.setQuestion(question);

        return answers;
    }
}
