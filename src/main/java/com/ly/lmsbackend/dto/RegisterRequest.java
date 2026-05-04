package com.ly.lmsbackend.dto;

public record RegisterRequest(
        String email,
        String username,
        String password
) {
}
