package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.RegisterRequest;
import com.ly.lmsbackend.dto.UserResponseDTO;
import com.ly.lmsbackend.dto.VerifyUserOtp;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
        UserResponseDTO user = userService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<UserResponseDTO> verifyUser(@RequestBody VerifyUserOtp verifyUserOtp) {
        UserResponseDTO user = userService.verifyOtp(verifyUserOtp.email(), verifyUserOtp.otp());
        return ResponseEntity.ok(user);
    }


    @GetMapping("/")
    public String greet(){
        return "Hello World";
    }


}
