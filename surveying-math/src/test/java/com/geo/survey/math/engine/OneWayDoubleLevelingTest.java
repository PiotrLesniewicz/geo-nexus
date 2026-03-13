package com.geo.survey.math.engine;

import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingResultReport;
import com.geo.survey.math.value.LevelingStationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OneWayDoubleLevelingTest {

    private OneWayDoubleLeveling oneWayDoubleLeveling;
    private Double startH;
    private Double endH;

    @BeforeEach
    void setUp() {
        oneWayDoubleLeveling = new OneWayDoubleLeveling();
        startH = 100.000;
        endH = 102.000;
    }

    @Test
    void shouldThrowException_ForEmptyData() {
        //given
        List<LevelingObservation> data = List.of();

        //when - then
        assertThatThrownBy(() -> oneWayDoubleLeveling.calculate(startH, endH, data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Raw data for leveling calculation cannot be empty");
    }

    @Test
    void shouldCalculateStationError_AsHalfDifferenceBetweenTwoMeasurements() {
        // given
        LevelingObservation obs = LevelingObservation.builder()
                .stationId("100")
                .backSightId("1")
                .foreSightId("2")
                .backsightElevation1(1.503)
                .foresightElevation1(1.098)
                .backsightElevation2(1.500)
                .foresightElevation2(1.098)
                .build();

        /*
         * Δh1 = 1.503 - 1.098 = 0.405
         * Δh2 = 1.500 - 1.098 = 0.402
         * stationError = (Δh1 - Δh2) / 2 = 0.0015
         */

        List<LevelingObservation> data = List.of(obs);

        // when
        LevelingResultReport result =
                new OneWayDoubleLeveling().calculate(startH, endH, data);

        // then
        assertThat(result.stationResults())
                .hasSize(1)
                .first()
                .satisfies(sr ->
                        assertThat(sr.stationError())
                                .as("Station error should be half the difference between two measurements")
                                .isCloseTo(0.0015, within(1e-6))
                );
    }

    @Test
    void shouldReturnInformationAboutToleranceExceeding_WhenMisclosureIsTooHigh() {
        //given
        /* expected misclosure from these observations,
         misclosure is measured difference - theoretical difference*/
        double expected = 0.0240;
        List<LevelingObservation> data = getLevelingObservationsForTooHighMisclosure();

        //when
        LevelingResultReport result = oneWayDoubleLeveling.calculate(startH, endH, data);

        //then
        assertThat(result.misclosure()).isCloseTo(expected, within(1e-6));
        assertThat(result.withTolerance()).isFalse();
        assertThat(Math.abs(result.misclosure()))
                .withFailMessage("Expected misclosure (%s) to be greater than allowed (%s)",
                        result.misclosure(), result.allowedMisclosure())
                .isGreaterThan(result.allowedMisclosure());
    }

    @Test
    void shouldCalculateAdjustedHeightLastStation_FromSequenceLeveling() {
        //given
        List<LevelingObservation> rawData = getLevelingObservationsWithoutDistance();

        //when
        LevelingResultReport result = oneWayDoubleLeveling.calculate(startH, endH, rawData);

        //then
        // last measurement equals endH (102.00)
        assertThat(result.stationResults()).isNotEmpty();
        assertThat(result.stationResults().getLast().adjustedHeight()).isCloseTo(102.0000, within(1e-6));
    }

    @Test
    void shouldCompensateMisclosure_WithAppliedCorrections() {
        // given
        List<LevelingObservation> data = getLevelingObservationsWithoutDistance();

        // when
        LevelingResultReport result = oneWayDoubleLeveling.calculate(startH, endH, data);

        // then
        //single correction = misclosure/countStation
        double totalCorrection = result.stationResults().stream()
                .mapToDouble(LevelingStationResult::correction)
                .sum();

        assertThat(totalCorrection)
                .as("Sum of all applied corrections should compensate the misclosure")
                .isCloseTo(-result.misclosure(), within(1e-6));
    }

    @Test
    void shouldCalculateMisclosure_FromSequenceLeveling() {
        //given
        List<LevelingObservation> rawData = getLevelingObservationsWithoutDistance();

        //when
        LevelingResultReport result = oneWayDoubleLeveling.calculate(startH, endH, rawData);

        //then
        assertThat(result.theoreticalDifference()).isEqualTo(2.000);
        assertThat(result.misclosure()).isCloseTo(-0.0025, within(1e-6));
    }

    private static List<LevelingObservation> getLevelingObservationsForTooHighMisclosure() {
        return List.of(
                LevelingObservation.builder()
                        .stationId("100").backSightId("1").foreSightId("2")
                        .backsightElevation1(1.515).foresightElevation1(1.098)
                        .backsightElevation2(1.515).foresightElevation2(1.098)
                        .build(),
                LevelingObservation.builder()
                        .stationId("101").backSightId("2").foreSightId("3")
                        .backsightElevation1(1.650).foresightElevation1(1.255)
                        .backsightElevation2(1.650).foresightElevation2(1.252)
                        .build(),
                LevelingObservation.builder()
                        .stationId("103").backSightId("3").foreSightId("4")
                        .backsightElevation1(2.002).foresightElevation1(1.577)
                        .backsightElevation2(2.002).foresightElevation2(1.598)
                        .build(),
                LevelingObservation.builder()
                        .stationId("104").backSightId("4").foreSightId("5")
                        .backsightElevation1(1.800).foresightElevation1(1.404)
                        .backsightElevation2(1.801).foresightElevation2(1.404)
                        .build(),
                LevelingObservation.builder()
                        .stationId("105").backSightId("5").foreSightId("6")
                        .backsightElevation1(1.678).foresightElevation1(1.300)
                        .backsightElevation2(1.723).foresightElevation2(1.302)
                        .build()
        );
    }

    private static List<LevelingObservation> getLevelingObservationsWithoutDistance() {
        return List.of(
                LevelingObservation.builder()
                        .stationId("100").backSightId("1").foreSightId("2")
                        .backsightElevation1(1.503).foresightElevation1(1.098)
                        .backsightElevation2(1.500).foresightElevation2(1.098)
                        .build(),
                LevelingObservation.builder()
                        .stationId("101").backSightId("2").foreSightId("3")
                        .backsightElevation1(1.650).foresightElevation1(1.255)
                        .backsightElevation2(1.650).foresightElevation2(1.252)
                        .build(),
                LevelingObservation.builder()
                        .stationId("103").backSightId("3").foreSightId("4")
                        .backsightElevation1(2.002).foresightElevation1(1.598)
                        .backsightElevation2(2.002).foresightElevation2(1.598)
                        .build(),
                LevelingObservation.builder()
                        .stationId("104").backSightId("4").foreSightId("5")
                        .backsightElevation1(1.800).foresightElevation1(1.404)
                        .backsightElevation2(1.801).foresightElevation2(1.404)
                        .build(),
                LevelingObservation.builder()
                        .stationId("105").backSightId("5").foreSightId("6")
                        .backsightElevation1(1.698).foresightElevation1(1.300)
                        .backsightElevation2(1.698).foresightElevation2(1.302)
                        .build()
        );
    }
}