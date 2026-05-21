package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.RegisterRequest;
import com.ly.lmsbackend.dto.ResetPasswordRequest;
import com.ly.lmsbackend.dto.UserResponseDTO;
import com.ly.lmsbackend.dto.VerifyUserOtp;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody RegisterRequest request) {

        Roles role = request.role();

        if (role == null) {
            role = Roles.USER;
        }

        if (role != Roles.USER && role != Roles.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }

        RegisterRequest registerRequest = new RegisterRequest(
                request.email(),
                request.username(),
                request.password(),
                role
        );

        UserResponseDTO user = userService.register(registerRequest);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/register")
    public ResponseEntity<UserResponseDTO> registerAdmin(@RequestBody RegisterRequest request) {
        UserResponseDTO user = userService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<UserResponseDTO> verifyUser(@RequestBody VerifyUserOtp verifyUserOtp) {
        UserResponseDTO user = userService.verifyOtp(verifyUserOtp.email(), verifyUserOtp.otp());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        try{
            userService.sendResetOtp(email);
        }
        catch(Exception e){
            throw new RuntimeException("Failed to send reset otp");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(
                    request.otp(),
                    request.email(),
                    request.password()
            );
            return ResponseEntity.ok("Password reset successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/")
    public String greet(){
        return "Hello World";
    }


}
