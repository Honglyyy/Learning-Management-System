package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.RegisterRequest;
import com.ly.lmsbackend.dto.UserResponseDTO;
import com.ly.lmsbackend.mapper.UserMapper;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.UserRepository;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponseDTO register(RegisterRequest request){
        if(userRepository.findByUsername(request.email()).isPresent()){
            throw new RuntimeException("Email already exists");
        }

        Users users = new Users();
        users.setUsername(request.username());
        users.setPassword(passwordEncoder.encode(request.password()));
        users.setEmail(request.email());
        users.setUserId(UUID.randomUUID().toString());


        return userMapper.dto(userRepository.save(users));
    }
}