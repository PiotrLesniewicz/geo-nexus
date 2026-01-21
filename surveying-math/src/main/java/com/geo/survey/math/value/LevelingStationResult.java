package com.geo.survey.math.value;

public record LevelingStationResult(
        LevelingObservation observation,
        Double heightDiffFirst,
        Double heightDiffSecond,
        double heightAvgDiff,
        double stationError,
        Boolean isStationWithinTolerance,
        double correction,
        double adjustedHeight
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LevelingObservation observation;
        private Double heightDiffFirst;
        private Double heightDiffSecond;
        private double heightAvgDiff;
        private double stationError;
        private Boolean isStationWithinTolerance;
        private double correction;
        private double adjustedHeight;

        public Builder observation(LevelingObservation observation) {
            this.observation = observation;
            return this;
        }

        public Builder heightDiffFirst(Double heightDiffFirst) {
            this.heightDiffFirst = heightDiffFirst;
            return this;
        }

        public Builder heightDiffSecond(Double heightDiffSecond) {
            this.heightDiffSecond = heightDiffSecond;
            return this;
        }

        public Builder heightAvgDiff(double heightAvgDiff) {
            this.heightAvgDiff = heightAvgDiff;
            return this;
        }

        public Builder stationError(double stationError) {
            this.stationError = stationError;
            return this;
        }

        public Builder isStationWithinTolerance(Boolean isStationWithinTolerance) {
            this.isStationWithinTolerance = isStationWithinTolerance;
            return this;
        }

        public Builder correction(double correction) {
            this.correction = correction;
            return this;
        }

        public Builder adjustedHeight(double adjustedHeight) {
            this.adjustedHeight = adjustedHeight;
            return this;
        }

        public LevelingStationResult build() {
            return new LevelingStationResult(
                    observation,
                    heightDiffFirst,
                    heightDiffSecond,
                    heightAvgDiff,
                    stationError,
                    isStationWithinTolerance,
                    correction,
                    adjustedHeight
            );
        }
    }
}