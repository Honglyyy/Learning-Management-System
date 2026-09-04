package com.ly.lmsbackend.dto.authdtos;

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
