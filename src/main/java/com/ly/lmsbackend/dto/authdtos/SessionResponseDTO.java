package com.ly.lmsbackend.dto.authdtos;

public record SessionResponseDTO(
        Boolean authenticated,
        Long userId,
        String email,
        String username,
        String role,
        Long expiresInSeconds
) {}
