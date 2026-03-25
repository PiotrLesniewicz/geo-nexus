package com.geo.survey.unit;

import com.geo.survey.domain.exception.ValidationDataException;
import com.geo.survey.domain.service.ValidationService;
import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    // Helper methods for creating valid observations

    private LevelingObservation validOneWay() {
        return LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(10)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .build();
    }

    private LevelingObservation validOneWayDouble() {
        return LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(10)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .backsightElevation2(1.230)
                .foresightElevation2(0.990)
                .build();
    }

    // Happy path tests - valid observations should pass validation

    @Test
    void validate_shouldPass_whenOneWayDataIsValid() {
        List<LevelingObservation> observations = List.of(validOneWay(), validOneWay());

        assertThatNoException().isThrownBy(
                () -> validationService.validate(observations, LevelingType.ONE_WAY));
    }

    @Test
    void validate_shouldPass_whenOneWayDoubleDataIsValid() {
        List<LevelingObservation> observations = List.of(validOneWayDouble(), validOneWayDouble());

        assertThatNoException().isThrownBy(
                () -> validationService.validate(observations, LevelingType.ONE_WAY_DOUBLE));
    }

    // Validation of empty observation list

    @Test
    void validate_shouldThrow_whenObservationListIsNull() {
        assertThatThrownBy(() -> validationService.validate(null, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void validate_shouldThrow_whenObservationListIsEmpty() {
        List<LevelingObservation> obs = List.of();
        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("empty");
    }

    // Validation of station IDs

    @Test
    void validate_shouldThrow_whenStationIdIsBlank() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(10)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("Station ID")
                .hasMessageContaining("[1]");
    }

    @Test
    void validate_shouldThrow_whenBackSightIdIsNull() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("ST1")
                .backSightId(null)
                .foreSightId("FS1")
                .backDistance(10)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("BackSight ID")
                .hasMessageContaining("[1]");
    }

    @Test
    void validate_shouldThrow_whenForeSightIdIsBlank() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("  ")
                .backDistance(10)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("ForeSight ID")
                .hasMessageContaining("[1]");
    }

    // Validation of distances

    @Test
    void validate_shouldThrow_whenBackDistanceIsZero() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(0)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("BackDistance")
                .hasMessageContaining("[1]");
    }

    @Test
    void validate_shouldThrow_whenBackDistanceIsNegative() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(-10)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("BackDistance")
                .hasMessageContaining("[1]");
    }

    @Test
    void validate_shouldThrow_whenForeDistanceIsNegative() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(10)
                .foreDistance(-5)
                .backsightElevation1(1.234)
                .foresightElevation1(0.987)
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("ForeDistance")
                .hasMessageContaining("[1]");
    }

    // Validation of elevation range

    @Test
    void validate_shouldThrow_whenElevationExceedsMaxRange() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(10)
                .foreDistance(12)
                .backsightElevation1(6.0) // exceeds 5m limit
                .foresightElevation1(0.987)
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("BacksightElevation1")
                .hasMessageContaining("meters");
    }

    @Test
    void validate_shouldThrow_whenElevationIsBelowMinRange() {
        List<LevelingObservation> obs = List.of(LevelingObservation.builder()
                .stationId("ST1")
                .backSightId("BS1")
                .foreSightId("FS1")
                .backDistance(10)
                .foreDistance(12)
                .backsightElevation1(1.234)
                .foresightElevation1(0.00001) // below minimum
                .build());

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("ForesightElevation1")
                .hasMessageContaining("meters");
    }

    // Validation of double type observations

    @Test
    void validate_shouldThrow_whenOneWayDoubleHasMissingSecondElevation() {
        List<LevelingObservation> obs = List.of(validOneWay()); // missing elevation2

        assertThatThrownBy(() -> validationService.validate(obs, LevelingType.ONE_WAY_DOUBLE))
                .isInstanceOf(ValidationDataException.class)
                .hasMessageContaining("ONE_WAY_DOUBLE")
                .hasMessageContaining("[1]");
    }

    @Test
    void validate_shouldPass_whenOneWayHasNoSecondElevation() {
        List<LevelingObservation> obs = List.of(validOneWay());

        assertThatNoException().isThrownBy(
                () -> validationService.validate(obs, LevelingType.ONE_WAY));
    }
}
