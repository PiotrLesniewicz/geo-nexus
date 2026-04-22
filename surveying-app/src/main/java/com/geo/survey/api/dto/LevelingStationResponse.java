package com.geo.survey.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record LevelingStationResponse(
        @Schema(description = "Station identifier", example = "ST1")
        String stationId,

        @Schema(description = "Backsight point identifier", example = "BS1")
        String backsightId,

        @Schema(description = "Foresight point identifier", example = "FS1")
        String foresightId,

        @Schema(description = "Distance to backsight point in meters", example = "45")
        Integer backDistance,

        @Schema(description = "Distance to foresight point in meters", example = "50")
        Integer foreDistance,

        @Schema(description = "Height difference from first reading (backsight - foresight) in meters", example = "0.0520")
        BigDecimal heightDiffFirst,

        @Schema(
                description = "Height difference from second reading in ONE_WAY_DOUBLE leveling in meters. Null for ONE_WAY",
                example = "0.0518"
        )
        BigDecimal heightDiffSecond,

        @Schema(
                description = "Average height difference from both readings in ONE_WAY_DOUBLE leveling in meters. Null for ONE_WAY",
                example = "0.0519"
        )
        BigDecimal heightAvgDiff,

        @Schema(
                description = "Difference between first and second reading — station error in ONE_WAY_DOUBLE leveling in meters. Null for ONE_WAY",
                example = "0.0002"
        )
        BigDecimal stationError,

        @Schema(description = "Indicates whether the station error is within allowed tolerance. Always true for ONE_WAY")
        boolean toleranceMet,

        @Schema(description = "Linear correction applied to the station height in meters", example = "-0.0001")
        BigDecimal correction,

        @Schema(description = "Final adjusted height of the foresight point after applying correction in meters", example = "100.0519")
        BigDecimal adjustedHeight
) {
}

