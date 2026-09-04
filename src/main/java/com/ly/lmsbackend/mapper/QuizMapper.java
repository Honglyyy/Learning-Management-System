package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.quizdtos.QuizCreateDTO;
import com.ly.lmsbackend.dto.quizdtos.QuizResponseDTO;
import com.ly.lmsbackend.model.Lessons;
import com.ly.lmsbackend.model.Quizzes;
import org.springframework.stereotype.Component;

@Component
public class QuizMapper {
    public QuizResponseDTO toDto(Quizzes quiz){
        return new QuizResponseDTO(
                quiz.getQuizId(),
                quiz.getQuizTitle(),
                quiz.getTotalPoint(),
                quiz.getLesson().getLessonId(),
                quiz.getLesson().getTitle()
        );
    }

    public Quizzes toEntity(
            QuizCreateDTO dto,
            Lessons lesson
    ){
        Quizzes quiz = new Quizzes();

        quiz.setQuizTitle(dto.title());
        quiz.setTotalPoint(dto.totalPoints());
        quiz.setLesson(lesson);

        return quiz;
    }
}