package com.geo.survey.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password must not be empty")
        String password
) {
}
