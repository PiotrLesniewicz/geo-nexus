package com.geo.survey.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank(message = "Old password must not be empty")
        String oldPassword,
        @NotBlank(message = "New password must not be empty")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters, contain uppercase, lowercase and digit"
        )
        String newPassword) {
}
