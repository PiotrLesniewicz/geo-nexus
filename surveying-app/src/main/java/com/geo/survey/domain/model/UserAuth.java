package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.OffsetDateTime;

@With
@Value
@Builder
public class UserAuth {

    Long id;
    String passwordHash;
    OffsetDateTime passwordChangedAt;
    boolean mustChange;
    OffsetDateTime createdAt;
    User user;
}