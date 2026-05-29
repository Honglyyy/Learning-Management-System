package com.ly.lmsbackend.dto;

import lombok.Builder;

@Builder
public record UserResponseDTO(
        Long id,
        String userId,
        String username,
        String email,
        String role,
        Boolean isVerified
) {
}
