package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.QuizCreateDTO;
import com.ly.lmsbackend.dto.QuizResponseDTO;
import com.ly.lmsbackend.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
