package com.geo.survey.api.dto;

import com.geo.survey.domain.model.StatusJob;

import java.time.OffsetDateTime;

public record JobListItemDto(
        String jobIdentifier,
        String city,
        String description,
        StatusJob status,
        OffsetDateTime createdAt
) {
}
