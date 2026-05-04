package com.ly.lmsbackend.dto;

import lombok.Builder;

@Builder
public record UserResponseDTO(
        String userId,
        String username,
        String email,
        Boolean isVerified
) {
}
