package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.authdtos.ChangePasswordRequest;
import com.ly.lmsbackend.dto.authdtos.RegisterRequest;
import com.ly.lmsbackend.dto.authdtos.ResetPasswordRequest;
import com.ly.lmsbackend.dto.authdtos.UpdateRoleRequest;
import com.ly.lmsbackend.dto.authdtos.UserResponseDTO;
import com.ly.lmsbackend.dto.authdtos.VerifyUserOtp;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody RegisterRequest request) {

        Roles role = request.role();

        if (role == null) {
            role = Roles.STUDENT;
        }

        if (role != Roles.STUDENT && role != Roles.USER && role != Roles.INSTRUCTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role. Allowed roles: STUDENT, INSTRUCTOR");
        }

        RegisterRequest registerRequest = new RegisterRequest(
                request.username(),
                request.email(),
                request.password(),
                request.fullName(),
                request.phoneNumber(),
                role
        );

        UserResponseDTO user = userService.register(registerRequest);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/register")
    public ResponseEntity<UserResponseDTO> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        UserResponseDTO user = userService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<UserResponseDTO> verifyUser(@Valid @RequestBody VerifyUserOtp verifyUserOtp) {
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
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/users/change-password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok("Password changed successfully");
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

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/users/{id}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateUserRole(id, request.role())
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
