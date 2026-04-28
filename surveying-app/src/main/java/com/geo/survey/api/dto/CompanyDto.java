package com.geo.survey.api.dto;

import java.time.OffsetDateTime;

public record CompanyDto(
        Long id,
        String name,
        String nip,
        boolean active,
        OffsetDateTime registerAt,
        OffsetDateTime blockedAt
) {
}
