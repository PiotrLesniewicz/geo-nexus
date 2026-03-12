package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Clock;
import java.time.OffsetDateTime;

@With
@Value
@Builder(toBuilder = true)
public class Job {

    Long id;
    String jobIdentifier;
    Address address;
    String description;
    StatusJob status;
    OffsetDateTime createdAt;
    Company company;
    User user;

    public static Job create(Job job, Company company, User user, Clock clock) {
        return job.toBuilder()
                .status(StatusJob.OPEN)
                .createdAt(OffsetDateTime.now(clock))
                .company(company)
                .user(user)
                .build();
    }
}
