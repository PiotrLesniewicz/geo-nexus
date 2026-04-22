package com.geo.survey.api.dto;

import com.geo.survey.math.value.LevelingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record LevelingReportResponse(
        @Schema(description = "Type of leveling measurement", example = "ONE_WAY")
        LevelingType levelingType,

        @Schema(description = "Starting benchmark height in meters", example = "100.0000")
        BigDecimal startHeight,

        @Schema(description = "Ending benchmark height in meters", example = "100.5000")
        BigDecimal endHeight,

        @Schema(description = "Measured height difference between start and end point in meters", example = "0.5020")
        BigDecimal measuredDifference,

        @Schema(description = "Theoretical height difference calculated from benchmarks in meters", example = "0.5000")
        BigDecimal theoreticalDifference,

        @Schema(description = "Misclosure — difference between measured and theoretical height difference in meters", example = "0.0020")
        BigDecimal misclosure,

        @Schema(description = "Maximum allowed misclosure based on total distance in meters", example = "0.0120")
        BigDecimal allowedMisclosure,

        @Schema(description = "Indicates whether the misclosure is within the allowed tolerance")
        boolean toleranceMet,

        @Schema(description = "Total leveling route distance in kilometers", example = "0.4860")
        BigDecimal sequenceDistance,

        @Schema(description = "Date and time of the field observation")
        OffsetDateTime observationTime,

        @Schema(description = "Date and time when the report was generated")
        OffsetDateTime generatedAt,

        @Schema(description = "Job identifier this report belongs to", example = "JOB-2024-001")
        String jobIdentifier,

        @Schema(description = "List of individual leveling stations with calculated values")
        List<LevelingStationResponse> stations
) {
}