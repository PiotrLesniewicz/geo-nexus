package com.geo.survey.api.dto;

import lombok.Builder;

@Builder
public record RegisterCompanyRequest(
        String companyName,
        String nip,
        String street,
        String buildingNumber,
        String apartmentNumber,
        String postalCode,
        String city,
        String county,
        String voivodeship,
        String country,
        String adminEmail,
        String adminName,
        String adminSurname,
        String password
) {
}
