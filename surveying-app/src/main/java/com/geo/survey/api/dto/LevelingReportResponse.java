package com.geo.survey.api.dto;

import com.geo.survey.math.value.LevelingType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record LevelingReportResponse(
        Long id,
        LevelingType levelingType,
        BigDecimal startHeight,
        BigDecimal endHeight,
        BigDecimal measuredDifference,
        BigDecimal theoreticalDifference,
        BigDecimal misclosure,
        BigDecimal allowedMisclosure,
        boolean toleranceMet,
        BigDecimal sequenceDistance,
        OffsetDateTime observationTime,
        OffsetDateTime generatedAt,
        String jobIdentifier,
        List<LevelingStationResponse> stations
) {}