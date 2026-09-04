package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.authdtos.AuthRequest;
import com.ly.lmsbackend.mapper.UserMapper;
import com.ly.lmsbackend.model.Roles;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.UserRepository;
import com.ly.lmsbackend.service.EmailService;
import com.ly.lmsbackend.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final UserRepository userRepository;


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserMapper userMapper, EmailService emailService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@Valid @RequestBody AuthRequest authRequest) throws Exception {
        Users isVerifiedUser = userRepository.findByEmail(authRequest.email())
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        if(isVerifiedUser.getIsVerified() ==  true){
            try{
                UsernamePasswordAuthenticationToken user  = new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password());
                authenticationManager.authenticate(user);
                System.out.println(jwtUtil.generateToken(isVerifiedUser));
                if(isVerifiedUser.getRole() != Roles.ADMIN){
                    emailService.sendWelcomeLogin(authRequest.email());
                }
                return ResponseEntity.ok(jwtUtil.generateToken(isVerifiedUser));
            }
            catch (Exception e){
                e.printStackTrace();
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }
        else{
            return ResponseEntity.badRequest().build();
        }
    }
}
