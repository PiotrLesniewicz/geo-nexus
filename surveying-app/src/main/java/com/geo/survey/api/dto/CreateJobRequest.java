package com.geo.survey.api.dto;

import lombok.Builder;

@Builder
public record CreateJobRequest(
        String jobIdentifier,
        String description,
        String street,
        String buildingNumber,
        String apartmentNumber,
        String postalCode,
        String city,
        String county,
        String voivodeship,
        String country
) {}
