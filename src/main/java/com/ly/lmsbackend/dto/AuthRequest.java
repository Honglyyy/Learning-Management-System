package com.ly.lmsbackend.dto;

public record AuthRequest(
        String email,
        String password
){}
