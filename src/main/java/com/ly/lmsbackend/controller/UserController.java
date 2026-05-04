package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.RegisterRequest;
import com.ly.lmsbackend.dto.UserResponseDTO;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody RegisterRequest request) {
        emailService.sendWelcomeEmail(request.email(), request.username());
        return ResponseEntity.ok(userService.register(request));
    }

    @GetMapping("/")
    public String greet(){
        return "Hello World";
    }


}
