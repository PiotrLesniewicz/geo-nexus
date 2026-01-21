package com.geo.survey.math.value;

import java.util.Optional;

public record LevelingObservation(
        String stationId,
        String backSightId,
        String foreSightId,
        int backDistance,
        int foreDistance,
        double backsightElevation1,
        double foresightElevation1,
        Double backsightElevation2,
        Double foresightElevation2
) {

    public double getDiffElevFirst() {
        return backsightElevation1 - foresightElevation1;
    }

    public Optional<Double> getDiffElevSecond() {
        if (backsightElevation2 == null || foresightElevation2 == null) {
            return Optional.empty();
        }
        return Optional.of(backsightElevation2 - foresightElevation2);
    }

    public boolean hasFullDistance() {
        return backDistance != 0 && backDistance > 0
                && foreDistance != 0 && foreDistance > 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String stationId;
        private String backSightId;
        private String foreSightId;
        private int backDistance;
        private int foreDistance;
        private double backsightElevation1;
        private double foresightElevation1;
        private Double backsightElevation2;
        private Double foresightElevation2;

        public Builder stationId(String stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder backSightId(String backSightId) {
            this.backSightId = backSightId;
            return this;
        }

        public Builder foreSightId(String foreSightId) {
            this.foreSightId = foreSightId;
            return this;
        }

        public Builder backDistance(int backDistance) {
            this.backDistance = backDistance;
            return this;
        }

        public Builder foreDistance(int foreDistance) {
            this.foreDistance = foreDistance;
            return this;
        }

        public Builder backsightElevation1(double backsightElevation1) {
            this.backsightElevation1 = backsightElevation1;
            return this;
        }

        public Builder foresightElevation1(double foresightElevation1) {
            this.foresightElevation1 = foresightElevation1;
            return this;
        }

        public Builder backsightElevation2(Double backsightElevation2) {
            this.backsightElevation2 = backsightElevation2;
            return this;
        }

        public Builder foresightElevation2(Double foresightElevation2) {
            this.foresightElevation2 = foresightElevation2;
            return this;
        }

        public LevelingObservation build() {
            return new LevelingObservation(
                    stationId,
                    backSightId,
                    foreSightId,
                    backDistance,
                    foreDistance,
                    backsightElevation1,
                    foresightElevation1,
                    backsightElevation2,
                    foresightElevation2
            );
        }
    }
}
