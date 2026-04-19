package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
public class JobListItem {
    String jobIdentifier;
    String city;
    String description;
    StatusJob status;
    OffsetDateTime createdAt;
}
