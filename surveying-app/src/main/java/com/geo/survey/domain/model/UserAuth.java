package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Clock;
import java.time.OffsetDateTime;

@With
@Value
@Builder(toBuilder = true)
public class UserAuth {

    Long id;
    String passwordHash;
    OffsetDateTime passwordChangedAt;
    boolean mustChange;
    OffsetDateTime createdAt;
    User user;

    public static UserAuth register(User user, String passwordHash, Clock clock) {
        return UserAuth.builder()
                .passwordHash(passwordHash)
                .passwordChangedAt(OffsetDateTime.now(clock))
                .mustChange(false)
                .createdAt(OffsetDateTime.now(clock))
                .user(user)
                .build();

    }
}