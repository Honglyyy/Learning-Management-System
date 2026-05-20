package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.AnswerCreateDTO;
import com.ly.lmsbackend.dto.AnswerResponseDTO;
import com.ly.lmsbackend.model.Answers;
import com.ly.lmsbackend.service.AnswerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class AnswerController {
    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/api/answers")
    ResponseEntity<List<AnswerResponseDTO>> getAnswers(){
        return new ResponseEntity<>(answerService.getAnswers(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PostMapping("/api/answers")
    ResponseEntity<AnswerResponseDTO> addAnswer(@RequestBody AnswerCreateDTO dto){
        return new ResponseEntity<>(answerService.addAnswer(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @PutMapping("/api/answers/{id}")
    ResponseEntity<Answers> updateAnswer(
            @PathVariable Long id,
            @RequestBody AnswerCreateDTO dto){
        return new ResponseEntity<>(answerService.updateAnswer(id,dto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @DeleteMapping("/api/answers/{id}")
    ResponseEntity<String> deleteAnswer(@PathVariable Long id ){
        answerService.deleteAnswer(id);
        return new ResponseEntity<>("Answer with id " + id + " is deleted successfully!!", HttpStatus.OK);
    }

}
