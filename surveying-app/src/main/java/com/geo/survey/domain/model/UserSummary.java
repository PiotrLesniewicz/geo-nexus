package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserSummary {
    User user;
    int countJob;
    int openJob;
}
