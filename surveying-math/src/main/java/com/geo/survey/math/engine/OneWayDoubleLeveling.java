package com.geo.survey.math.engine;

import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingResultReport;
import com.geo.survey.math.value.LevelingStationResult;
import com.geo.survey.math.value.LevelingType;

import java.util.ArrayList;
import java.util.List;

import static com.geo.survey.math.engine.LevelingMath.*;

public class OneWayDoubleLeveling implements LevelingEngine {

    @Override
    public LevelingType getType() {
        return LevelingType.ONE_WAY_DOUBLE;
    }

    @Override
    public LevelingResultReport calculate(
            final Double startH,
            final Double endH,
            final List<LevelingObservation> data
    ) {
        validate(data);

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
                .toleranceMet(isWithinTolerance)
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
            var hdf = obs.getDiffElevFirst();
            var hds = obs.getDiffElevSecond().orElse(0.0);
            var se = getStationError(obs);
            var ste = isStationWithinTolerance(se);
            var hd = heightDifference.get(i);
            var ah = getAdjustedHeight(currentH, hd, correction);
            stationResults.add(LevelingStationResult.builder()
                    .observation(obs)
                    .heightDiffFirst(hdf)
                    .heightDiffSecond(hds)
                    .heightAvgDiff(hd)
                    .stationError(se)
                    .toleranceMet(ste)
                    .correction(correction)
                    .adjustedHeight(ah)
                    .build());
            currentH = ah;
        }
        return stationResults;
    }
}