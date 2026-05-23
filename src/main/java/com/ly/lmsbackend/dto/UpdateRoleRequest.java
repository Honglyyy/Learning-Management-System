package com.ly.lmsbackend.dto;

import com.ly.lmsbackend.model.Roles;

public record UpdateRoleRequest(
        Roles role
) {
}