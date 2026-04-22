package com.geo.survey.api.dto;

import com.geo.survey.api.validation.NotSuperAdmin;
import com.geo.survey.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(
        @NotNull(message = "Role must not be null")
        @NotSuperAdmin
        @Schema(allowableValues = {"ADMIN", "SURVEYOR"})
        Role role
) {
}
