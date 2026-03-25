package com.geo.survey.unit.parser;


import com.geo.survey.domain.exception.ParsingException;
import com.geo.survey.infrastructure.parser.CsvLevelingFileParser;
import com.geo.survey.math.value.LevelingObservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvLevelingFileParserTest {
    private CsvLevelingFileParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvLevelingFileParser();
    }

    // supports()

    @Test
    void supports_shouldReturnTrue_whenCsvExtension() {
        assertThat(parser.supports("data.csv")).isTrue();
    }

    @Test
    void supports_shouldReturnFalse_whenTxtExtension() {
        assertThat(parser.supports("data.txt")).isFalse();
    }

    @Test
    void supports_shouldReturnFalse_whenNoExtension() {
        assertThat(parser.supports("com/geo/survey/testdata")).isFalse();
    }

    @Test
    void supports_shouldReturnFalse_whenCsvInMiddleButOtherExtension() {
        assertThat(parser.supports("data.csv.txt")).isFalse();
    }

    // parse() - happy path

    @Test
    void parse_shouldReturnObservations_whenOneWayCsv() throws ParsingException {
        InputStream stream = getStream("one_way.csv");

        List<LevelingObservation> result = parser.parse(stream, "one_way.csv");

        assertThat(result).hasSize(3);

        LevelingObservation first = result.getFirst();
        assertThat(first.stationId()).isEqualTo("ST1");
        assertThat(first.backSightId()).isEqualTo("BS1");
        assertThat(first.foreSightId()).isEqualTo("FS1");
        assertThat(first.backDistance()).isEqualTo(10);
        assertThat(first.foreDistance()).isEqualTo(12);
        assertThat(first.backsightElevation1()).isEqualTo(1.234);
        assertThat(first.foresightElevation1()).isEqualTo(0.987);
        assertThat(first.backsightElevation2()).isNull();
        assertThat(first.foresightElevation2()).isNull();
    }

    @Test
    void parse_shouldReturnObservations_whenOneWayDoubleCsv() throws ParsingException {
        InputStream stream = getStream("one_way_double.csv");

        List<LevelingObservation> result = parser.parse(stream, "one_way_double.csv");

        assertThat(result).hasSize(3);

        LevelingObservation first = result.getFirst();
        assertThat(first.backsightElevation2()).isEqualTo(1.230);
        assertThat(first.foresightElevation2()).isEqualTo(0.990);
    }

    @Test
    void parse_shouldIgnoreBlankLines() throws ParsingException {
        InputStream stream = getStream("blank_lines.csv");

        List<LevelingObservation> result = parser.parse(stream, "blank_lines.csv");

        assertThat(result).hasSize(2);
    }

    // parse() - error cases

    @Test
    void parse_shouldThrowParserException_whenFileIsEmpty() {
        InputStream stream = getStream("empty.csv");

        assertThatThrownBy(() -> parser.parse(stream, "empty.csv"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void parse_shouldThrowParserException_whenInvalidColumnCount() {
        InputStream stream = getStream("invalid_columns.csv");

        assertThatThrownBy(() -> parser.parse(stream, "invalid_columns.csv"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("line [1]");
    }

    @Test
    void parse_shouldThrowParserException_whenInvalidNumericValue() {
        InputStream stream = getStream("invalid_number.csv");

        assertThatThrownBy(() -> parser.parse(stream, "invalid_number.csv"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("[1]: [For input string: \"abc\"]");
    }

    // helper

    private InputStream getStream(String filename) {
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("data_pars/csv/" + filename);
        assertThat(stream).as("Missing test file:  " + filename).isNotNull();
        return stream;
    }
}