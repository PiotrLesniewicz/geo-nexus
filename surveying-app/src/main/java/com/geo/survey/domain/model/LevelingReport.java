package com.geo.survey.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@With
@Value
@Builder
public class LevelingReport {

    Long id;
    LevelingType levelingType;
    BigDecimal startHeight;
    BigDecimal endHeight;
    BigDecimal measuredDifference;
    BigDecimal theoreticalDifference;
    BigDecimal misclosure;
    BigDecimal allowedMisclosure;
    boolean isWithinTolerance;
    BigDecimal sequenceDistance;
    OffsetDateTime observationTime;
    OffsetDateTime generatedAt;
    List<LevelingStation> stations;
    Job job;
}
