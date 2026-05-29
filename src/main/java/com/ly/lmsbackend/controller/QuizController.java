package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.QuizDetailDTO;
import com.ly.lmsbackend.dto.QuizAttemptResponseDTO;
import com.ly.lmsbackend.dto.QuizCreateDTO;
import com.ly.lmsbackend.dto.QuizResponseDTO;
import com.ly.lmsbackend.dto.QuizSubmitDTO;
import com.ly.lmsbackend.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/api/quizzes")
    public ResponseEntity<List<QuizResponseDTO>> getQuizzes(){
        return new ResponseEntity<>(quizService.getQuizzes(), HttpStatus.OK);
    }

    @GetMapping("/api/lessons/{lessonId}/quiz")
    public ResponseEntity<QuizDetailDTO> getQuizByLesson(@PathVariable Long lessonId) {
        return new ResponseEntity<>(quizService.getQuizByLesson(lessonId), HttpStatus.OK);
    }

    @GetMapping("/api/quizzes/{id}/attempt/me")
    public ResponseEntity<QuizAttemptResponseDTO> getMyQuizAttempt(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(quizService.getMyAttempt(id, authentication.getName()));
    }

    @PostMapping("/api/quizzes/{id}/submit")
    public ResponseEntity<QuizAttemptResponseDTO> submitQuiz(
            @PathVariable Long id,
            @RequestBody QuizSubmitDTO dto,
            Authentication authentication
    ) {
        return ResponseEntity.ok(quizService.submitQuiz(id, dto, authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/quizzes")
    public ResponseEntity<QuizResponseDTO> addQuiz(@RequestBody QuizCreateDTO dto){
        return new ResponseEntity<>(quizService.addQuiz(dto),HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/quizzes/{id}")
    public ResponseEntity<QuizResponseDTO> updateQuiz(
            @PathVariable Long id,
            @RequestBody QuizCreateDTO dto
    ){
        return new ResponseEntity<>(quizService.updateQuiz(id,dto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/quizzes/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id){
        quizService.deleteQuiz(id);
        return new ResponseEntity<>("Quiz id " + id + " is now deleted!!", HttpStatus.OK);
    }
}
