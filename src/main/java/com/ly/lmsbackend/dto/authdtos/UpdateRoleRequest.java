package com.ly.lmsbackend.dto.authdtos;

import com.ly.lmsbackend.model.Roles;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "Role is required")
        Roles role
) {
}