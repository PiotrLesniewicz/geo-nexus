package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.OffsetDateTime;

@With
@Value
@Builder(toBuilder = true)
public class Company {

    Long id;
    String name;
    String nip;
    Address address;
    boolean active;
    OffsetDateTime registerAt;
    OffsetDateTime blockedAt;
}
