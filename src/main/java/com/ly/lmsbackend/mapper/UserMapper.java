package com.ly.lmsbackend.mapper;

import com.ly.lmsbackend.dto.authdtos.AuthRequest;
import com.ly.lmsbackend.dto.authdtos.RegisterRequest;
import com.ly.lmsbackend.dto.authdtos.UserResponseDTO;
import com.ly.lmsbackend.model.Users;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {
    public UserResponseDTO dto(
            Users user
    ){
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .userId(user.getUserId())
                .role(user.getRole().name())
                .isVerified(user.getIsVerified())
                .build();
    }

}
