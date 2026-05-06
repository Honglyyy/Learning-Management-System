package com.ly.lmsbackend.dto;

import com.ly.lmsbackend.model.Roles;

public record RegisterRequest(
        String email,
        String username,
        String password,
        Roles role
) {
}
