package com.geo.survey.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteJobRequest(
        @NotBlank(message = "Old password must not be empty")
        String password,
        @NotBlank(message = "Job Identifier must not be empty")
        String jobIdentifier
) {
}
