package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.domain.model.LevelingStation;
import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingResultReport;
import com.geo.survey.math.value.LevelingStationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class LevelingResultMapper {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public LevelingReport toDomain(LevelingResultReport result) {
        return LevelingReport.builder()
                .startHeight(toBigDecimal(result.startH()))
                .endHeight(toBigDecimal(result.endH()))
                .measuredDifference(toBigDecimal(result.measuredDifference()))
                .theoreticalDifference(toBigDecimal(result.theoreticalDifference()))
                .misclosure(toBigDecimal(result.misclosure()))
                .allowedMisclosure(toBigDecimal(result.allowedMisclosure()))
                .sequenceDistance(toBigDecimal(result.sequenceDistance()))
                .isWithinTolerance(result.withTolerance())
                .stations(toStations(result.stationResults()))
                .build();
    }

    private List<LevelingStation> toStations(List<LevelingStationResult> stationResults) {
        return stationResults.stream()
                .map(this::toStation)
                .toList();
    }

    private LevelingStation toStation(LevelingStationResult result) {
        LevelingObservation obs = result.observation();
        return LevelingStation.builder()
                .stationId(obs.stationId())
                .backsightId(obs.backSightId())
                .foresightId(obs.foreSightId())
                .backDistance(obs.backDistance())
                .foreDistance(obs.foreDistance())
                .backsightElev1(toBigDecimal(obs.backsightElevation1()))
                .foresightElev1(toBigDecimal(obs.foresightElevation1()))
                .backsightElev2(toBigDecimal(obs.backsightElevation2()))
                .foresightElev2(toBigDecimal(obs.foresightElevation2()))
                .heightDiffFirst(toBigDecimal(result.heightDiffFirst()))
                .heightDiffSecond(toBigDecimal(result.heightDiffSecond()))
                .heightAvgDiff(toBigDecimal(result.heightAvgDiff()))
                .stationError(toBigDecimal(result.stationError()))
                .withTolerance(Boolean.TRUE.equals(result.isStationWithinTolerance()))
                .correction(toBigDecimal(result.correction()))
                .adjustedHeight(toBigDecimal(result.adjustedHeight()))
                .build();
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(SCALE, ROUNDING);
    }

    private BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, ROUNDING);
    }
}
