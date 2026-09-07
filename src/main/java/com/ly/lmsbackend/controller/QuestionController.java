package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.questiondtos.QuestionCreateDTO;
import com.ly.lmsbackend.dto.questiondtos.QuestionResponseDTO;
import com.ly.lmsbackend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/api/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestions(){
        return new ResponseEntity<>(questionService.getQuestions(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/questions")
    public ResponseEntity<QuestionResponseDTO> addQuestion(@Valid @RequestBody QuestionCreateDTO dto){
        return new ResponseEntity<>(questionService.addQuesion(dto),HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionCreateDTO dto
    ){
        return new ResponseEntity<>(questionService.updateQuestion(id,dto),HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/questions/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id){
        questionService.deleteQuestion(id);
        return new ResponseEntity<>("Question id " + id + " is now deleted!!", HttpStatus.OK );
    }
}
