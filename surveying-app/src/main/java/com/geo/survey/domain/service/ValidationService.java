package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.ValidationDataException;
import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationService {

    private static final double MIN_ELEVATION = 0.0001;
    private static final double MAX_ELEVATION = 5.0;

    public void validate(List<LevelingObservation> observations, LevelingType type) {
        validateNotEmpty(observations);
        validateStationIds(observations);
        validateDistances(observations);
        validateElevationRange(observations);
        validateDoubleType(observations, type);
    }

    private void validateNotEmpty(List<LevelingObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            throw new ValidationDataException("Observation list cannot be empty");
        }
    }

    private void validateStationIds(List<LevelingObservation> observations) {
        for (int i = 0; i < observations.size(); i++) {
            LevelingObservation obs = observations.get(i);
            if (isBlank(obs.stationId())) {
                throw new ValidationDataException(
                        "Station ID is blank at observation [%d]".formatted(i + 1));
            }
            if (isBlank(obs.backSightId())) {
                throw new ValidationDataException(
                        "BackSight ID is blank at observation [%d]".formatted(i + 1));
            }
            if (isBlank(obs.foreSightId())) {
                throw new ValidationDataException(
                        "ForeSight ID is blank at observation [%d]".formatted(i + 1));
            }
        }
    }

    private void validateDistances(List<LevelingObservation> observations) {
        for (int i = 0; i < observations.size(); i++) {
            LevelingObservation obs = observations.get(i);
            if (obs.backDistance() <= 0) {
                throw new ValidationDataException(
                        "BackDistance must be greater than 0 at observation [%d]".formatted(i + 1));
            }
            if (obs.foreDistance() <= 0) {
                throw new ValidationDataException(
                        "ForeDistance must be greater than 0 at observation [%d]".formatted(i + 1));
            }
        }
    }

    private void validateElevationRange(List<LevelingObservation> observations) {
        for (int i = 0; i < observations.size(); i++) {
            LevelingObservation obs = observations.get(i);
            validateSingleElevation(obs.backsightElevation1(), "BacksightElevation1", i + 1);
            validateSingleElevation(obs.foresightElevation1(), "ForesightElevation1", i + 1);
            if (obs.backsightElevation2() != null) {
                validateSingleElevation(obs.backsightElevation2(), "BacksightElevation2", i + 1);
            }
            if (obs.foresightElevation2() != null) {
                validateSingleElevation(obs.foresightElevation2(), "ForesightElevation2", i + 1);
            }
        }
    }

    private void validateSingleElevation(double value, String fieldName, int index) {
        if (value < MIN_ELEVATION || value > MAX_ELEVATION) {
            throw new ValidationDataException(
                    "[%s] value [%.4f] out of valid range [%.4f - %.1f] at observation [%d]. Check if data is in meters"
                            .formatted(fieldName, value, MIN_ELEVATION, MAX_ELEVATION, index));
        }
    }

    private void validateDoubleType(List<LevelingObservation> observations, LevelingType type) {
        if (type != LevelingType.ONE_WAY_DOUBLE) {
            return;
        }

        for (int i = 0; i < observations.size(); i++) {
            LevelingObservation obs = observations.get(i);
            if (obs.backsightElevation2() == null || obs.foresightElevation2() == null) {
                throw new ValidationDataException(
                        "ONE_WAY_DOUBLE requires second elevation readings at observation [%d]".formatted(i + 1));
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
