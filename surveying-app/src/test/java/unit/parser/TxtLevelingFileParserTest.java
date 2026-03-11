package unit.parser;

import com.geo.survey.domain.exception.ParsingException;
import com.geo.survey.infrastructure.parser.TxtLevelingFileParser;
import com.geo.survey.math.value.LevelingObservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TxtLevelingFileParserTest {
    private TxtLevelingFileParser parser;

    @BeforeEach
    void setUp() {
        parser = new TxtLevelingFileParser();
    }

    // supports()

    @Test
    void supports_shouldReturnTrue_whenTxtExtension() {
        assertThat(parser.supports("data.txt")).isTrue();
    }

    @Test
    void supports_shouldReturnFalse_whenCsvExtension() {
        assertThat(parser.supports("data.csv")).isFalse();
    }

    @Test
    void supports_shouldReturnFalse_whenNoExtension() {
        assertThat(parser.supports("data")).isFalse();
    }

    // parse() - happy path

    @Test
    void parse_shouldReturnObservations_whenOneWayTxt() throws ParsingException {
        InputStream stream = getStream("one_way.txt");

        List<LevelingObservation> result = parser.parse(stream, "one_way.txt");

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
    void parse_shouldReturnObservations_whenOneWayDoubleTxt() throws ParsingException {
        InputStream stream = getStream("one_way_double.txt");

        List<LevelingObservation> result = parser.parse(stream, "one_way_double.txt");

        assertThat(result).hasSize(3);

        LevelingObservation first = result.getFirst();
        assertThat(first.backsightElevation2()).isEqualTo(1.230);
        assertThat(first.foresightElevation2()).isEqualTo(0.990);
    }

    @Test
    void parse_shouldHandleMultipleSpacesBetweenColumns() throws ParsingException {
        InputStream stream = getStream("multiple_spaces.txt");

        List<LevelingObservation> result = parser.parse(stream, "multiple_spaces.txt");

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().stationId()).isEqualTo("ST1");
    }

    // parse() - error cases

    @Test
    void parse_shouldThrowParserException_whenFileIsEmpty() {
        InputStream stream = getStream("empty.txt");

        assertThatThrownBy(() -> parser.parse(stream, "empty.txt"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("empty.txt");
    }

    @Test
    void parse_shouldThrowParserException_whenInvalidColumnCount() {
        InputStream stream = getStream("invalid_columns.txt"); // 6 columns - fits both parsers

        assertThatThrownBy(() -> parser.parse(stream, "invalid_columns.txt"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("line [1]");
    }

    @Test
    void parse_shouldThrowParserException_whenInvalidNumericValue() {
        InputStream stream = getStream("invalid_number.txt");

        assertThatThrownBy(() -> parser.parse(stream, "invalid_number.txt"))
                .isInstanceOf(ParsingException.class)
                .hasMessageContaining("line [1]");
    }

    // helper

    private InputStream getStream(String filename) {
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("data_pars/txt/" + filename);
        assertThat(stream).as("Missing test file: " + filename).isNotNull();
        return stream;
    }
}
