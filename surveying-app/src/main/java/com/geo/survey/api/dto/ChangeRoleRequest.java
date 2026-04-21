package com.geo.survey.api.dto;

import com.geo.survey.api.validation.NotSuperAdmin;
import com.geo.survey.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull(message = "Role must not be null")
        @NotSuperAdmin
        Role role
) {
}
