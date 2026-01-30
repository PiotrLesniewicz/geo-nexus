package com.geo.survey.math.engine;

import com.geo.survey.math.value.LevelingObservation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LevelingMathTest {

    @ParameterizedTest(name = "misclosure={0}, allowed={1} → withinTolerance={2}")
    @MethodSource("getMisclosureToleranceTestCases")
    @DisplayName("Should check if misclosure is within allowed tolerance")
    void shouldCheckIfMisclosureIsWithinTolerance(double misclosure, double allowedMisclosure, boolean expected) {
        //given, when
        boolean result = LevelingMath.isMisclosureWithinTolerance(misclosure, allowedMisclosure);

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void calculateAllowedMisclosure_WhenProvideDistance() {
        //given
        // formula: 20mm x root(L_km) -> 0.02 * sqrt(465/1000) = 0.013638178
        double expected = 0.013638178;
        List<LevelingObservation> data = getLevelingObservationsWithDistance();

        //when
        double result = LevelingMath.getAllowedMisclosure(data);

        //then
        assertThat(result).isCloseTo(expected, within(1e-6));
    }

    @Test
    void calculateAllowedMisclosure_WhenNotProvideDistance() {
        //given
        // formula: 4mm x root(stationCount) -> 0.004 * sqrt(5) = 0.00894427191
        double expected = 0.00894427191;
        List<LevelingObservation> data = getLevelingObservationsWithoutDistance();

        //when
        double result = LevelingMath.getAllowedMisclosure(data);

        //then
        assertThat(result).isCloseTo(expected, within(1e-6));
    }

    @Test
    void shouldReturnSumSequenceDistance_WhenProvideDistance() {
        //given
        List<LevelingObservation> data = getLevelingObservationsWithDistance();

        //when
        double result = LevelingMath.getSequenceDistance(data);

        //then
        assertThat(result).isCloseTo(0.465, within(1e-6));
    }

    @Test
    void shouldReturnZeroSequenceDistance_WhenNotProvideDistance() {
        //given
        List<LevelingObservation> data = getLevelingObservationsWithoutDistance();

        //when
        double result = LevelingMath.getSequenceDistance(data);

        //then
        assertThat(result).isCloseTo(0.0, within(1e-6));
    }

    @ParameterizedTest(name = "Test {index}: stationError={1}m")
    @MethodSource("getDoubleMeasurementForStation")
    @DisplayName("Should calculate station error correctly for double measurements")
    void shouldReturnStationError_WhenDoubleMeasurement(LevelingObservation obs, double expected) {
        //given, when
        double result = LevelingMath.getStationError(obs);

        //then
        assertThat(result).isCloseTo(expected, within(1e-6));
    }

    @Test
    void shouldReturnStationErrorZero_WhenSingleMeasurement() {
        //given
        LevelingObservation obs = LevelingObservation.builder()
                .backsightElevation1(1.503).foresightElevation1(1.096) // only first measurement
                .build();

        // when
        double result = LevelingMath.getStationError(obs);

        //then
        assertThat(result).isCloseTo(0.000, within(1e-6));
    }

    @ParameterizedTest(name = "stationError={0} → withinTolerance={1}")
    @MethodSource("getStationToleranceTestCases")
    void shouldCheckIfStationIsWithinTolerance(double stationError, boolean expected) {
        //given, when
        boolean result = LevelingMath.isStationWithinTolerance(stationError);

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldValidateEmptyData_WhenNoDataProvided() {
        //given
        List<LevelingObservation> data = List.of();

        //when - then
        Assertions.assertThatThrownBy(() -> LevelingMath.validate(data))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Raw data for leveling calculation cannot be empty");
    }

    @ParameterizedTest
    @CsvSource({
            "0.010, 5, -0.002",  // Misclosure +10mm on 5 stations -> correction -2mm/station
            "-0.015, 3, 0.005",  // Misclosure -15mm on 3 stations -> correction +5mm/station
            "0.000, 10, 0.000"   // No misclosure -> no correction
    })
    @DisplayName("Should return correction with opposite sign distributed across all stations")
    void shouldCalculateSingleCorrectionWithOppositeSign(double misclosure, int stationCount, double expectedCorrection) {
        //given, when
        double result = LevelingMath.getSingleCorrection(stationCount, misclosure);

        //then
        assertThat(result)
                .as("Correction should be %s for misclosure %s and %s stations", expectedCorrection, misclosure, stationCount)
                .isCloseTo(expectedCorrection, within(1e-6));
    }

    @Test
    void shouldCalculateHeightDifferencesForSingleMeasurements() {
        //given
        List<LevelingObservation> data = List.of(
                LevelingObservation.builder()
                        .backsightElevation1(1.500).foresightElevation1(1.100)  // diff = 0.400
                        .build(),
                LevelingObservation.builder()
                        .backsightElevation1(1.650).foresightElevation1(1.250)  // diff = 0.400
                        .build(),
                LevelingObservation.builder()
                        .backsightElevation1(2.000).foresightElevation1(1.600)  // diff = 0.400
                        .build()
        );

        //when
        List<Double> result = LevelingMath.getHeightDifference(data);

        //then
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isCloseTo(0.400, within(1e-6));
        assertThat(result.get(1)).isCloseTo(0.400, within(1e-6));
        assertThat(result.get(2)).isCloseTo(0.400, within(1e-6));
    }

    @Test
    void shouldCalculateHeightDifferencesForDoubleMeasurements_UsingAverage() {
        //given
        List<LevelingObservation> data = List.of(
                LevelingObservation.builder()
                        .backsightElevation1(1.503).foresightElevation1(1.096)  // diff1 = 0.407
                        .backsightElevation2(1.502).foresightElevation2(1.098)  // diff2 = 0.404
                        .build(),  // average = 0.4055
                LevelingObservation.builder()
                        .backsightElevation1(1.650).foresightElevation1(1.255)  // diff1 = 0.395
                        .backsightElevation2(1.650).foresightElevation2(1.255)  // diff2 = 0.395
                        .build()   // average = 0.395
        );

        //when
        List<Double> result = LevelingMath.getHeightDifference(data);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isCloseTo(0.4055, within(1e-6));  // (0.407 + 0.404) / 2
        assertThat(result.get(1)).isCloseTo(0.395, within(1e-6));
    }

    @Test
    void shouldCalculateHeightDifferencesForMixedMeasurements() {
        //given
        List<LevelingObservation> data = List.of(
                // Single measurement - only elevation1
                LevelingObservation.builder()
                        .backsightElevation1(1.500).foresightElevation1(1.100)
                        .build(),
                // Double measurement - average of both
                LevelingObservation.builder()
                        .backsightElevation1(1.650).foresightElevation1(1.250)
                        .backsightElevation2(1.652).foresightElevation2(1.252)
                        .build()
        );

        //when
        List<Double> result = LevelingMath.getHeightDifference(data);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isCloseTo(0.400, within(1e-6));  // single
        assertThat(result.get(1)).isCloseTo(0.400, within(1e-6));  // average: (0.400 + 0.400) / 2
    }

    @ParameterizedTest(name = "heightDifferences={0} → sum={1}")
    @MethodSource("getMeasuredDifferenceTestCases")
    @DisplayName("Should calculate sum of height differences (measured difference)")
    void shouldCalculateMeasuredDifferenceFromHeightDifferences(List<Double> heightDifferences, double expectedSum) {
        //given, when
        double result = LevelingMath.getMeasuredDifference(heightDifferences);

        //then
        assertThat(result).isCloseTo(expectedSum, within(1e-6));
    }

    private static Stream<Arguments> getMeasuredDifferenceTestCases() {
        return Stream.of(
                // Positive values
                Arguments.of(List.of(0.400, 0.395, 0.404), 1.199),

                // Negative values
                Arguments.of(List.of(-0.400, -0.395, -0.404), -1.199),

                // Mixed positive and negative
                Arguments.of(List.of(0.400, -0.200, 0.300), 0.500),

                // Single value
                Arguments.of(List.of(0.400), 0.400),

                // Zero sum
                Arguments.of(List.of(0.500, -0.500), 0.000),

                // Empty list
                Arguments.of(List.of(), 0.000)
        );
    }

    private static List<LevelingObservation> getLevelingObservationsWithDistance() {
        return List.of(
                LevelingObservation.builder()
                        .stationId("100").backSightId("1").foreSightId("2")
                        .backDistance(42).foreDistance(45)
                        .backsightElevation1(1.503).foresightElevation1(1.098)
                        .build(),
                LevelingObservation.builder()
                        .stationId("101").backSightId("2").foreSightId("3")
                        .backDistance(50).foreDistance(48)
                        .backsightElevation1(1.650).foresightElevation1(1.255)
                        .build(),
                LevelingObservation.builder()
                        .stationId("103").backSightId("3").foreSightId("4")
                        .backDistance(60).foreDistance(55)
                        .backsightElevation1(2.002).foresightElevation1(1.598)
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

    private static Stream<Arguments> getMisclosureToleranceTestCases() {
        return Stream.of(
                // Within tolerance - positive misclosure
                Arguments.of(0.005, 0.010, true),
                Arguments.of(0.010, 0.010, true),  // exactly at tolerance
                Arguments.of(0.000, 0.010, true),  // zero misclosure

                // Within tolerance - negative misclosure
                Arguments.of(-0.005, 0.010, true),
                Arguments.of(-0.010, 0.010, true), // exactly at tolerance (negative)

                // Outside tolerance - positive misclosure
                Arguments.of(0.011, 0.010, false),
                Arguments.of(0.020, 0.010, false),

                // Outside tolerance - negative misclosure
                Arguments.of(-0.011, 0.010, false),
                Arguments.of(-0.020, 0.010, false),

                // Edge case - very small allowed tolerance
                Arguments.of(0.0001, 0.0001, true),
                Arguments.of(0.0002, 0.0001, false)
        );
    }

    private static Stream<Arguments> getDoubleMeasurementForStation() {
        return Stream.of(
                Arguments.of(
                        LevelingObservation.builder()
                                .backsightElevation1(1.503).foresightElevation1(1.096) // back - fores
                                .backsightElevation2(1.502).foresightElevation2(1.098)
                                .build(),
                        0.0015 // first - second
                ),
                Arguments.of(
                        LevelingObservation.builder()
                                .backsightElevation1(1.018).foresightElevation1(1.590)
                                .backsightElevation2(1.021).foresightElevation2(1.591)
                                .build(),
                        -0.001
                ),
                Arguments.of(
                        LevelingObservation.builder()
                                .backsightElevation1(1.121).foresightElevation1(1.591)
                                .backsightElevation2(1.121).foresightElevation2(1.591)
                                .build(),
                        0.000
                )
        );
    }

    private static Stream<Arguments> getStationToleranceTestCases() {
        double tolerance = 0.004; // Must match LevelingMath.TOLERANCE_FACTOR_PER_STATION

        return Stream.of(
                // Within tolerance - positive values
                Arguments.of(0.000, true),
                Arguments.of(0.001, true),
                Arguments.of(0.002, true),
                Arguments.of(tolerance, true), // exactly at tolerance

                // Within tolerance - negative values
                Arguments.of(-0.001, true),
                Arguments.of(-0.002, true),
                Arguments.of(-tolerance, true), // exactly at tolerance

                // Outside tolerance - positive values
                Arguments.of(0.005, false),
                Arguments.of(tolerance + 0.0001, false), // max tolerance error 0.00001

                // Outside tolerance - negative values
                Arguments.of(-0.005, false),
                Arguments.of(-tolerance - 0.0001, false)
        );
    }
}