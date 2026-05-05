package com.ly.lmsbackend.controller;

import com.ly.lmsbackend.dto.AuthRequest;
import com.ly.lmsbackend.mapper.UserMapper;
import com.ly.lmsbackend.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthRequest authRequest) throws Exception {
        UsernamePasswordAuthenticationToken user  = new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password());

        try{
            authenticationManager.authenticate(user);
            System.out.println(jwtUtil.generateToken(userMapper.toEntity(authRequest)));
            return ResponseEntity.ok(jwtUtil.generateToken(userMapper.toEntity(authRequest)));
        }
        catch (Exception e){
            IO.println("Authentication Failed");
            return ResponseEntity.badRequest().build();
        }
    }
}
