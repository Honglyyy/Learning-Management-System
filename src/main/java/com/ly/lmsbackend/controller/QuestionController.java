package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.QuestionCreateDTO;
import com.ly.lmsbackend.dto.QuestionResponseDTO;
import com.ly.lmsbackend.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/api/questions")
    public ResponseEntity<QuestionResponseDTO> addQuestion(@RequestBody QuestionCreateDTO dto){
        return new ResponseEntity<>(questionService.addQuesion(dto),HttpStatus.CREATED);
    }

    @PutMapping("/api/questions/{id}")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(
            @PathVariable Long id,
            @RequestBody QuestionCreateDTO dto
    ){
        return new ResponseEntity<>(questionService.updateQuestion(id,dto),HttpStatus.OK);
    }

    @DeleteMapping("/api/questions/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id){
        questionService.deleteQuestion(id);
        return new ResponseEntity<>("Question id " + id + " is now deleted!!", HttpStatus.OK );
    }
}
