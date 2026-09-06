package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.authdtos.AuthRequest;
import com.ly.lmsbackend.mapper.UserMapper;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.UserRepository;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@Valid @RequestBody AuthRequest authRequest) {
        Users isVerifiedUser = userRepository.findByEmail(authRequest.email())
                .or(() -> userRepository.findByPhoneNumber(authRequest.email()))
                .or(() -> userRepository.findByUsername(authRequest.email()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (Boolean.TRUE.equals(isVerifiedUser.getIsVerified())) {
            try {
                UsernamePasswordAuthenticationToken user =
                        new UsernamePasswordAuthenticationToken(isVerifiedUser.getEmail(), authRequest.password());
                authenticationManager.authenticate(user);
                if (isVerifiedUser.getRole() != Roles.ADMIN) {
                    emailService.sendWelcomeLogin(isVerifiedUser.getEmail());
                }
                return ResponseEntity.ok(jwtUtil.generateToken(isVerifiedUser));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } else {
            return ResponseEntity.badRequest().body("User account is not verified");
        }
    }
}
