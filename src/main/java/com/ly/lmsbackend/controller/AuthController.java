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
import com.ly.lmsbackend.dto.authdtos.SessionResponseDTO;
import com.ly.lmsbackend.service.ActivityLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

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
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

            try {
                if (isVerifiedUser.getRole() != Roles.ADMIN) {
                    emailService.sendWelcomeLogin(isVerifiedUser.getEmail());
                }
            } catch (Exception e) {
                // Non-fatal: email failure must not break login
            }

            try {
                if (activityLogService != null) {
                    activityLogService.logActivity(isVerifiedUser, "USER_LOGIN", "User logged in to LMS");
                }
            } catch (Exception e) {
                // Non-fatal: activity log failure must not break login
            }

            return ResponseEntity.ok(jwtUtil.generateToken(isVerifiedUser));
        } else {
            return ResponseEntity.badRequest().body("User account is not verified");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/auth/session")
    public ResponseEntity<SessionResponseDTO> getSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SessionResponseDTO(false, null, null, null, null, 0L));
        }
        Users user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(new SessionResponseDTO(
                true,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole() != null ? user.getRole().name() : null,
                86400L
        ));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/auth/logout")
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication) {
        if (authentication != null && activityLogService != null) {
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                activityLogService.logActivity(user, "USER_LOGOUT", "User logged out of LMS");
            });
        }
        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
