package com.geo.survey.api.dto;

public record LoginResponseDto(
        String token,
        String type
) {
}
