package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.OffsetDateTime;

@With
@Value
@Builder
public class Job {

    Long id;
    String jobIdentifier;
    Address address;
    String description;
    OffsetDateTime createdAt;
    Company company;
    User user;
}
