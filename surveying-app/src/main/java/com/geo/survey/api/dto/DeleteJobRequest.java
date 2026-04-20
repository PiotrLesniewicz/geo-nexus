package com.geo.survey.api.dto;

public record DeleteJobRequest(
        String password,
        String jobIdentifier
) {
}
