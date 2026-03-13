package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;

@With
@Value
@Builder
public class LevelingStation {

    String stationId;
    String backsightId;
    String foresightId;
    Integer backDistance;
    Integer foreDistance;
    BigDecimal backsightElev1;
    BigDecimal foresightElev1;
    BigDecimal backsightElev2;
    BigDecimal foresightElev2;
    BigDecimal heightDiffFirst;
    BigDecimal heightDiffSecond;
    BigDecimal heightAvgDiff;
    BigDecimal stationError;
    boolean withTolerance;
    BigDecimal correction;
    BigDecimal adjustedHeight;
}
