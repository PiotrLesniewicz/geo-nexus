package com.geo.survey.api.dto;

import com.geo.survey.api.validation.NotSuperAdmin;
import com.geo.survey.domain.model.Role;
import jakarta.validation.constraints.*;

public record RegisterUserRequest(
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Name must not be empty")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Surname must not be empty")
        @Size(max = 100)
        String surname,

        @NotNull(message = "Role must not be null")
        @NotSuperAdmin
        Role role,

        @NotBlank(message = "Password must not be empty")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters, contain uppercase, lowercase and digit"
        )
        String password
) {
}
