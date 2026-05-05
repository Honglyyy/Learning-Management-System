package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.AuthRequest;
import com.ly.lmsbackend.dto.RegisterRequest;
import com.ly.lmsbackend.dto.UserResponseDTO;
import com.ly.lmsbackend.model.Users;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {
    public UserResponseDTO dto(
            Users user
    ){
        return UserResponseDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .userId(user.getUserId())
                .role(String.valueOf(user.getRole()))
                .isVerified(user.getIsVerified())
                .build();
    }

    public Users toEntity(
            AuthRequest authRequest
    ){
        return Users.builder()
                .email(authRequest.email())
                .password(authRequest.password())
                .build();
    }
}
