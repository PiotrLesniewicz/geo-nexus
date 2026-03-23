package com.geo.survey.api.dto;

import java.math.BigDecimal;

public record LevelingStationResponse(
        String stationId,
        String backsightId,
        String foresightId,
        Integer backDistance,
        Integer foreDistance,
        BigDecimal heightDiffFirst,
        BigDecimal heightDiffSecond,
        BigDecimal heightAvgDiff,
        BigDecimal stationError,
        boolean toleranceMet,
        BigDecimal correction,
        BigDecimal adjustedHeight
) {}

