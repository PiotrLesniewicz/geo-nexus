package com.geo.survey.api.dto;

import com.geo.survey.domain.model.StatusJob;
import java.time.OffsetDateTime;

public record JobResponse(
        Long id,
        String jobIdentifier,
        String description,
        StatusJob status,
        String street,
        String buildingNumber,
        String apartmentNumber,
        String postalCode,
        String city,
        String county,
        String voivodeship,
        String country,
        OffsetDateTime createdAt,
        Long companyId,
        Long userId
) {}
