package com.geo.survey.api.dto;

import com.geo.survey.domain.model.Role;

public record RegisterUserRequest(
        String email,
        String name,
        String surname,
        Role role,
        String password
) {
}
