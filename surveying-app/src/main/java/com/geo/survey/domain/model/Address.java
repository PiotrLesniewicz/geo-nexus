package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@With
@Value
@Builder
public class Address {

    String street;
    String buildingNumber;
    String apartmentNumber;
    String postalCode;
    String city;
    String county;
    String voivodeship;
    String country;
}