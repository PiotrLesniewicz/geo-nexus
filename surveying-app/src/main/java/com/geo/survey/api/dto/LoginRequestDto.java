package com.geo.survey.api.dto;

public record LoginRequestDto(
        String email,
        String password
) {
}
