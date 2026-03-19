package com.geo.survey.math.engine;

import com.geo.survey.math.value.LevelingObservation;

import java.util.List;

final class LevelingMath {

    private LevelingMath() {
    }

    private static final double TOLERANCE_FACTOR_PER_KILOMETER = 0.0200;
    private static final double TOLERANCE_FACTOR_PER_STATION = 0.0040;
    private static final double EPSILON = 0.00001;
    private static final double METER_TO_KILOMETER = 1000.0;

    static void validate(List<LevelingObservation> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Raw data for leveling calculation cannot be null or empty");
        }
    }

    static double getAdjustedHeight(double h, double hd, double correction) {
        return h + (hd + correction);
    }

    /**
     * Calculate the single correction to be applied to each station in a leveling sequence.
     * <p>
     * The misclosure is defined as:
     * <pre>
     *     misclosure = sum of measured height differences - (end height - start height)
     * </pre>
     * The single correction is the misclosure distributed evenly across all stations.
     * The negative sign indicates that the correction is applied in the opposite direction
     * to reduce the misclosure.
     *
     * @param stationCount number of stations in the leveling sequence
     * @param misclosure   the total misclosure of the sequence (in meters)
     * @return the correction to apply to each station (in meters)
     */
    static double getSingleCorrection(int stationCount, double misclosure) {
        double correction = misclosure / stationCount;
        return -correction;
    }

    static boolean isMisclosureWithinTolerance(double misclosure, double allowedMisclosure) {
        return Math.abs(misclosure) <= allowedMisclosure;
    }

    /**
     * Calculate allowed misclosure based on provided data.
     * If distance information is available for all observations, use distance-based formula:
     * 0.020 * sqrt(L_km) when 0.020 - const expressed in meters, L_km is the total distance for sequence in kilometers.
     * <p>
     * Otherwise, use station count-based formula:
     * 0.004 * sqrt(N) when 0.004 - const expressed in meters, N is the number of stations (observations).
     *
     * @param data List of LevelingObservation
     * @return allowed misclosure value
     */
    static double getAllowedMisclosure(List<LevelingObservation> data) {
        double sequenceDistance = getSequenceDistance(data);
        if (sequenceDistance > EPSILON) {
            return TOLERANCE_FACTOR_PER_KILOMETER * Math.sqrt(sequenceDistance);
        }
        int countStation = data.size();
        return TOLERANCE_FACTOR_PER_STATION * Math.sqrt(countStation);

    }

    static double getSequenceDistance(List<LevelingObservation> data) {
        if (hasDistance(data)) {
            return data.stream()
                    .mapToDouble(obs -> (obs.backDistance() + obs.foreDistance()) / METER_TO_KILOMETER)
                    .sum();
        }
        return 0.0; //0.0 is a valid measured value,
        // missing second measurement must be handled during validation.
    }

    static List<Double> getHeightDifference(final List<LevelingObservation> data) {
        return data.stream()
                .map(LevelingMath::getHeightDifferenceValue)
                .toList();
    }

    static double getMeasuredDifference(final List<Double> heightDifference) {
        return heightDifference.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    static double getStationError(final LevelingObservation obs) {
        return obs.getDiffElevSecond()
                .map(second -> (obs.getDiffElevFirst() - second) / 2)
                .orElse(0.0); //0.0 is a valid measured value,
        // missing second measurement must be handled during validation.
    }

    static boolean isStationWithinTolerance(double se) {
        return Math.abs(se) <= (TOLERANCE_FACTOR_PER_STATION + EPSILON);
    }

    private static double getHeightDifferenceValue(LevelingObservation obs) {
        return obs.getDiffElevSecond()
                .map(second -> (obs.getDiffElevFirst() + second) / 2)
                .orElseGet(obs::getDiffElevFirst);
    }

    private static boolean hasDistance(List<LevelingObservation> data) {
        return data.stream()
                .allMatch(LevelingObservation::hasFullDistance);
    }
}