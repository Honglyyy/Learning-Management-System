package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.questiondtos.QuestionCreateDTO;
import com.ly.lmsbackend.dto.questiondtos.QuestionResponseDTO;
import com.ly.lmsbackend.model.Questions;
import com.ly.lmsbackend.model.Quizzes;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public QuestionResponseDTO toDto(Questions question){
        return new QuestionResponseDTO(
                question.getQuestionId(),
                question.getQuestionText(),
                question.getPoint(),
                question.getQuiz().getQuizId()
        );
    }

    public Questions toEntity(
            QuestionCreateDTO dto,
            Quizzes quiz
    ){
        Questions question = new Questions();

        question.setQuestionText(dto.questionText());
        question.setPoint(dto.point());
        question.setQuiz(quiz);

        return question;
    }
}
