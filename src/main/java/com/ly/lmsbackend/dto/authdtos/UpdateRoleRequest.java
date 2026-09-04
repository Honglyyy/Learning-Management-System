package com.ly.lmsbackend.dto.authdtos;

import com.ly.lmsbackend.model.Roles;

public record UpdateRoleRequest(
        Roles role
) {
}