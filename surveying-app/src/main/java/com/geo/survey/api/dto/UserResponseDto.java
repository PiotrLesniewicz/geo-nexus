package com.geo.survey.api.dto;

import com.geo.survey.domain.model.Role;

import java.time.OffsetDateTime;

public record UserResponseDto(
        String email,
        String name,
        String surname,
        Role role,
        boolean active,
        OffsetDateTime registerAt,
        OffsetDateTime deletedAt,
        int countJob,
        int openJob
) {
}
