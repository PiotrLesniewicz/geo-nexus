package com.geo.survey.domain.model;

import com.geo.survey.math.value.LevelingType;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@With
@Value
@Builder(toBuilder = true)
public class LevelingReport {

    Long id;
    LevelingType levelingType;
    BigDecimal startHeight;
    BigDecimal endHeight;
    BigDecimal measuredDifference;
    BigDecimal theoreticalDifference;
    BigDecimal misclosure;
    BigDecimal allowedMisclosure;
    boolean toleranceMet;
    BigDecimal sequenceDistance;
    OffsetDateTime observationTime;
    OffsetDateTime generatedAt;
    List<LevelingStation> stations;
    Job job;

    public static LevelingReport generated(
            LevelingReport levelingReport,
            LevelingType type,
            Job job,
            OffsetDateTime observationTime,
            Clock clock) {
        return levelingReport.toBuilder()
                .levelingType(type)
                .job(job)
                .observationTime(observationTime)
                .generatedAt(OffsetDateTime.now(clock))
                .build();
    }
}
