package com.geo.survey.math.engine;

import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingResultReport;
import com.geo.survey.math.value.LevelingStationResult;
import com.geo.survey.math.value.LevelingType;

import java.util.ArrayList;
import java.util.List;

import static com.geo.survey.math.engine.LevelingMath.*;

public class OneWayLeveling implements LevelingEngine {

    @Override
    public LevelingType getType() {
        return LevelingType.ONE_WAY;
    }

    @Override
    public LevelingResultReport calculate(
            final Double startH,
            final Double endH,
            final List<LevelingObservation> data
    ) {
        validate(data);
        return (startH == null || endH == null)
                ? calculateRelative(data)
                : calculateAbsolute(startH, endH, data);
    }

    private LevelingResultReport calculateRelative(List<LevelingObservation> data) {
        var heightDifference = getHeightDifference(data);
        var measuredDifference = getMeasuredDifference(heightDifference);
        var stationResults = applyLevelingCorrections(0.0, data, heightDifference, 0.0);
        var sequenceDistance = getSequenceDistance(data);
        var allowedMisclosure = getAllowedMisclosure(data);

        return LevelingResultReport.builder()
                .stationResults(stationResults)
                .measuredDifference(measuredDifference)
                .allowedMisclosure(allowedMisclosure)
                .sequenceDistance(sequenceDistance)
                .build();
    }

    private LevelingResultReport calculateAbsolute(
            final Double startH,
            final Double endH,
            final List<LevelingObservation> data
    ) {
        var heightDifference = getHeightDifference(data);
        var measuredDifference = getMeasuredDifference(heightDifference);
        var theoreticalDifference = endH - startH;
        var misclosure = measuredDifference - theoreticalDifference;
        var stationResults = applyLevelingCorrections(startH, data, heightDifference, misclosure);
        var allowedMisclosure = getAllowedMisclosure(data);
        var sequenceDistance = getSequenceDistance(data);
        boolean isWithinTolerance = isMisclosureWithinTolerance(misclosure, allowedMisclosure);

        return LevelingResultReport.builder()
                .startH(startH)
                .endH(endH)
                .stationResults(stationResults)
                .theoreticalDifference(theoreticalDifference)
                .measuredDifference(measuredDifference)
                .misclosure(misclosure)
                .allowedMisclosure(allowedMisclosure)
                .sequenceDistance(sequenceDistance)
                .isWithinTolerance(isWithinTolerance)
                .build();
    }

    private List<LevelingStationResult> applyLevelingCorrections(
            final double startH,
            final List<LevelingObservation> data,
            final List<Double> heightDifference,
            double misclosure
    ) {
        double currentH = startH;
        int stationCount = data.size();
        double correction = getSingleCorrection(stationCount, misclosure);
        List<LevelingStationResult> stationResults = new ArrayList<>();
        for (int i = 0; i < stationCount; i++) {
            var obs = data.get(i);
            var hd = heightDifference.get(i);
            var ah = getAdjustedHeight(currentH, hd, correction);
            stationResults.add(LevelingStationResult.builder()
                    .observation(obs)
                    .heightAvgDiff(hd)
                    .correction(correction)
                    .adjustedHeight(ah)
                    .build());
            currentH = ah;
        }
        return stationResults;
    }
}