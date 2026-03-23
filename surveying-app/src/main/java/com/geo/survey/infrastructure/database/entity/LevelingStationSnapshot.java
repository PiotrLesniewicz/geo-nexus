package com.geo.survey.infrastructure.database.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class LevelingStationSnapshot {

    String stationId;
    String backsightId;
    String foresightId;

    Integer backDistance;
    Integer foreDistance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal backsightElev1;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal foresightElev1;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal backsightElev2;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal foresightElev2;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal heightDiffFirst;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal heightDiffSecond;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal heightAvgDiff;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal stationError;

    boolean toleranceMet;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal correction;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal adjustedHeight;
}
