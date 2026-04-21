package com.geo.survey.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterCompanyRequest(
        @NotBlank(message = "Company name must not be empty")
        @Size(max = 255, message = "Company name must not exceed 255 characters")
        String companyName,

        @NotBlank(message = "NIP must not be empty")
        @Pattern(regexp = "\\d{10}", message = "NIP must contain exactly 10 digits")
        String nip,

        @Size(max = 150, message = "Street must not exceed 150 characters")
        String street,

        @Size(max = 20, message = "Building number must not exceed 20 characters")
        String buildingNumber,

        @Size(max = 20, message = "Apartment number must not exceed 20 characters")
        String apartmentNumber,

        @Pattern(regexp = "\\d{2}-\\d{3}", message = "Postal code must be in format XX-XXX")
        String postalCode,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Size(max = 100, message = "County must not exceed 100 characters")
        String county,

        @Size(max = 100, message = "Voivodeship must not exceed 100 characters")
        String voivodeship,

        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,

        @NotBlank(message = "Admin email must not be empty")
        @Email(message = "Admin email must be valid")
        String adminEmail,

        @NotBlank(message = "Admin name must not be empty")
        @Size(max = 100)
        String adminName,

        @NotBlank(message = "Admin surname must not be empty")
        @Size(max = 100)
        String adminSurname,

        @NotBlank(message = "Password must not be empty")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters, contain uppercase, lowercase and digit"
        )
        String password
) {
}
