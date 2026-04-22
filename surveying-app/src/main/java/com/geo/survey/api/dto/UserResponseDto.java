package com.geo.survey.api.dto;

import com.geo.survey.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record UserResponseDto(
        String email,
        String name,
        String surname,
        @Schema(allowableValues = {"ADMIN", "SURVEYOR"})
        Role role,
        boolean active,
        OffsetDateTime registerAt,
        OffsetDateTime deletedAt,
        int countJob,
        int openJob
) {
}
