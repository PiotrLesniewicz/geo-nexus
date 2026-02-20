package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.OffsetDateTime;

@With
@Value
@Builder
public class User {

    Long id;
    String email;
    String name;
    String surname;
    Role role;
    boolean active;
    OffsetDateTime registerAt;
    OffsetDateTime deletedAt;
    Company company;
}
