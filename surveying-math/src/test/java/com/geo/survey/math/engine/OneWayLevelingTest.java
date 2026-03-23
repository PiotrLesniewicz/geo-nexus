package com.geo.survey.math.engine;

import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingResultReport;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OneWayLevelingTest {

    private OneWayLeveling oneWayLeveling;
    private Double startH;
    private Double endH;

    @BeforeEach
    void setUp() {
        oneWayLeveling = new OneWayLeveling();
        startH = 100.000;
        endH = 102.000;
    }

    @Test
    void shouldThrowException_ForEmptyData() {
        //given
        List<LevelingObservation> data = List.of();

        //when - then
        assertThatThrownBy(() -> oneWayLeveling.calculate(startH, endH, data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Raw data for leveling calculation cannot be null or empty");
    }

    @Test
    void shouldReturnHeightDifference_WhenStartAndEndHeightNotProvided() {
        //given
        List<LevelingObservation> data = getLevelingObservationsWithoutDistance();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(null, null, data);

        //then
        assertThat(result.theoreticalDifference()).isEqualTo(0.0);
        assertThat(result.measuredDifference()).isCloseTo(1.9980, within(1e-6));
    }

    @Test
    void shouldReturnZeroForStationError() {
        // With only one observation at a station, no internal consistency check is possible,
        // therefore the station error must be zero

        //given
        List<LevelingObservation> data = getLevelingObservationsWithoutDistance();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(startH, endH, data);

        //then
        assertThat(result.stationResults()).isNotEmpty()
                .allSatisfy(stationResult -> assertThat(stationResult.stationError()).isEqualTo(0.0));
    }

    @Test
    void shouldCorrectlyCalculateDistance() {
        //given
        List<LevelingObservation> data = getLevelingObservationsWithDistance();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(startH, endH, data);

        //then
        assertThat(result.sequenceDistance()).isCloseTo(0.465, within(1e-6));
    }

    @Test
    void shouldCalculateMisclosureFromSequenceLeveling_WithDistance() {
        //given
        List<LevelingObservation> data = getLevelingObservationsWithDistance();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(startH, endH, data);

        //then
        assertThat(result.misclosure()).isCloseTo(-0.0030, within(1e-6));
        assertThat(result.toleranceMet()).isTrue();
    }

    @Test
    void shouldCalculateMisclosureFromSequenceLeveling_WithoutDistance() {
        //given
        List<LevelingObservation> rawData = getLevelingObservationsWithoutDistance();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(startH, endH, rawData);

        //then
        assertThat(result.misclosure()).isCloseTo(-0.0020, within(1e-6));
        assertThat(result.toleranceMet()).isTrue();
    }

    @Test
    void shouldReturnInformationAboutToleranceExceeding_WhenMisclosureIsTooHigh() {
        //given
        double expected = 0.0110;
        List<LevelingObservation> data = getLevelingObservationsForTooHighMisclosure();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(startH, endH, data);

        //then
        assertThat(result.misclosure()).isCloseTo(expected, within(1e-6));
        assertThat(result.toleranceMet()).isFalse();
        assertThat(Math.abs(result.misclosure()))
                .withFailMessage("Expected misclosure (%s) to be greater than allowed (%s)",
                        result.misclosure(), result.allowedMisclosure())
                .isGreaterThan(result.allowedMisclosure());
    }

    @Test
    void shouldCalculateAdjustedHeight_FromSequenceLeveling() {
        //given
        List<LevelingObservation> rawData = getLevelingObservationsWithoutDistance();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(startH, endH, rawData);

        //then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.stationResults()).hasSize(5);
            softly.assertThat(result.stationResults().get(0).adjustedHeight()).isCloseTo(100.4054, within(1e-6));
            softly.assertThat(result.stationResults().get(1).adjustedHeight()).isCloseTo(100.8008, within(1e-6));
            softly.assertThat(result.stationResults().get(2).adjustedHeight()).isCloseTo(101.2052, within(1e-6));
            softly.assertThat(result.stationResults().get(3).adjustedHeight()).isCloseTo(101.6016, within(1e-6));
            softly.assertThat(result.stationResults().get(4).adjustedHeight()).isCloseTo(102.0000, within(1e-6));
        });
    }

    @Test
    void shouldDistributeMisclosureLinearlyAcrossAllStations() {
        //given
        List<LevelingObservation> rawData = getLevelingObservationsWithoutDistance();

        //when
        LevelingResultReport result = oneWayLeveling.calculate(startH, endH, rawData);

        //then
        double firstCorrection = result.stationResults().getFirst().correction();

        assertThat(result.stationResults())
                .allSatisfy(sr ->
                        assertThat(sr.correction())
                                .isCloseTo(firstCorrection, within(1e-6))
                );
    }

    private static List<LevelingObservation> getLevelingObservationsForTooHighMisclosure() {
        return List.of(
                LevelingObservation.builder()
                        .stationId("100").backSightId("1").foreSightId("2")
                        .backsightElevation1(1.515).foresightElevation1(1.098)
                        .build(),
                LevelingObservation.builder()
                        .stationId("101").backSightId("2").foreSightId("3")
                        .backsightElevation1(1.650).foresightElevation1(1.255)
                        .build(),
                LevelingObservation.builder()
                        .stationId("103").backSightId("3").foreSightId("4")
                        .backsightElevation1(2.002).foresightElevation1(1.577)
                        .build(),
                LevelingObservation.builder()
                        .stationId("104").backSightId("4").foreSightId("5")
                        .backsightElevation1(1.800).foresightElevation1(1.404)
                        .build(),
                LevelingObservation.builder()
                        .stationId("105").backSightId("5").foreSightId("6")
                        .backsightElevation1(1.678).foresightElevation1(1.300)
                        .build()
        );
    }

    private static List<LevelingObservation> getLevelingObservationsWithoutDistance() {
        return List.of(
                LevelingObservation.builder()
                        .stationId("100").backSightId("1").foreSightId("2")
                        .backsightElevation1(1.503).foresightElevation1(1.098)
                        .build(),
                LevelingObservation.builder()
                        .stationId("101").backSightId("2").foreSightId("3")
                        .backsightElevation1(1.650).foresightElevation1(1.255)
                        .build(),
                LevelingObservation.builder()
                        .stationId("103").backSightId("3").foreSightId("4")
                        .backsightElevation1(2.002).foresightElevation1(1.598)
                        .build(),
                LevelingObservation.builder()
                        .stationId("104").backSightId("4").foreSightId("5")
                        .backsightElevation1(1.800).foresightElevation1(1.404)
                        .build(),
                LevelingObservation.builder()
                        .stationId("105").backSightId("5").foreSightId("6")
                        .backsightElevation1(1.698).foresightElevation1(1.300)
                        .build()
        );
    }

    private static List<LevelingObservation> getLevelingObservationsWithDistance() {
        return List.of(
                LevelingObservation.builder()
                        .stationId("100").backSightId("1").foreSightId("2")
                        .backDistance(42).foreDistance(45)
                        .backsightElevation1(1.50).foresightElevation1(1.098)
                        .build(),
                LevelingObservation.builder()
                        .stationId("101").backSightId("2").foreSightId("3")
                        .backDistance(50).foreDistance(48)
                        .backsightElevation1(1.650).foresightElevation1(1.258)
                        .build(),
                LevelingObservation.builder()
                        .stationId("103").backSightId("3").foreSightId("4")
                        .backDistance(60).foreDistance(55)
                        .backsightElevation1(2.007).foresightElevation1(1.598)
                        .build(),
                LevelingObservation.builder()
                        .stationId("104").backSightId("4").foreSightId("5")
                        .backDistance(38).foreDistance(40)
                        .backsightElevation1(1.800).foresightElevation1(1.404)
                        .build(),
                LevelingObservation.builder()
                        .stationId("105").backSightId("5").foreSightId("6")
                        .backDistance(45).foreDistance(42)
                        .backsightElevation1(1.698).foresightElevation1(1.300)
                        .build()
        );
    }
}