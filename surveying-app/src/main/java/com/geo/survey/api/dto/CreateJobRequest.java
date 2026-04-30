package com.geo.survey.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateJobRequest(
        @NotBlank(message = "Job identifier must not be blank")
        @Size(max = 100, message = "Job identifier must not exceed 100 characters")
        String jobIdentifier,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Size(max = 150, message = "Street must not exceed 150 characters")
        String street,

        @Size(max = 20, message = "Building number must not exceed 20 characters")
        String buildingNumber,

        @Size(max = 20, message = "Apartment number must not exceed 20 characters")
        String apartmentNumber,

        @Pattern(
                regexp = "\\d{2}-\\d{3}",
                message = "Postal code must be in format XX-XXX"
        )
        String postalCode,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Size(max = 100, message = "County must not exceed 100 characters")
        String county,

        @Size(max = 100, message = "Voivodeship must not exceed 100 characters")
        String voivodeship,

        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country
) {
}
