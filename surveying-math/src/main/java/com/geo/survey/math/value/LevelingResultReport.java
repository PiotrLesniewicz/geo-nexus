package com.geo.survey.math.value;

import java.util.List;

public record LevelingResultReport(
        Double startH,
        Double endH,
        List<LevelingStationResult> stationResults,
        double theoreticalDifference,
        double measuredDifference,
        double misclosure,
        double allowedMisclosure,
        double sequenceDistance,
        boolean isWithinTolerance
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Double startH;
        private Double endH;
        private List<LevelingStationResult> stationResults;
        private double theoreticalDifference;
        private double measuredDifference;
        private double misclosure;
        private double allowedMisclosure;
        private double sequenceDistance;
        private boolean isWithinTolerance;

        public Builder startH(Double startH) {
            this.startH = startH;
            return this;
        }

        public Builder endH(Double endH) {
            this.endH = endH;
            return this;
        }

        public Builder stationResults(List<LevelingStationResult> stationResults) {
            this.stationResults = stationResults;
            return this;
        }

        public Builder theoreticalDifference(double theoreticalDifference) {
            this.theoreticalDifference = theoreticalDifference;
            return this;
        }

        public Builder measuredDifference(double measuredDifference) {
            this.measuredDifference = measuredDifference;
            return this;
        }

        public Builder misclosure(double misclosure) {
            this.misclosure = misclosure;
            return this;
        }

        public Builder allowedMisclosure(double allowedMisclosure) {
            this.allowedMisclosure = allowedMisclosure;
            return this;
        }

        public Builder sequenceDistance(double sequenceDistance) {
            this.sequenceDistance = sequenceDistance;
            return this;
        }

        public Builder isWithinTolerance(boolean isWithinTolerance) {
            this.isWithinTolerance = isWithinTolerance;
            return this;
        }

        public LevelingResultReport build() {
            return new LevelingResultReport(
                    startH,
                    endH,
                    stationResults,
                    theoreticalDifference,
                    measuredDifference,
                    misclosure,
                    allowedMisclosure,
                    sequenceDistance,
                    isWithinTolerance
            );
        }
    }
}