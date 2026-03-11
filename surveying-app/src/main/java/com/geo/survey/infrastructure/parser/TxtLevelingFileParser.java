package com.geo.survey.infrastructure.parser;

import com.geo.survey.domain.exception.ParsingException;
import com.geo.survey.domain.service.LevelingFileParser;
import com.geo.survey.math.value.LevelingObservation;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class TxtLevelingFileParser implements LevelingFileParser {

    private static final int COLUMNS_ONE_WAY = 7;
    private static final int COLUMNS_ONE_WAY_DOUBLE = 9;

    @Override
    public List<LevelingObservation> parse(InputStream inputStream, String filename) throws ParsingException {
        List<LevelingObservation> observations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;

                String[] columns = line.trim().split("\\s+");

                if (columns.length != COLUMNS_ONE_WAY && columns.length != COLUMNS_ONE_WAY_DOUBLE) {
                    throw new ParsingException(
                            "Invalid number of columns at line [%s]. Expected [%s] or [%s], got: [%s]"
                                    .formatted(lineNumber, COLUMNS_ONE_WAY, COLUMNS_ONE_WAY_DOUBLE, columns.length)
                    );
                }

                observations.add(parseLine(columns, lineNumber));
            }

        } catch (IOException e) {
            throw new ParsingException("Failed to read file: [%s], [%s]".formatted(filename, e));
        }

        if (observations.isEmpty()) {
            throw new ParsingException("File is empty or contains no data: [%s]".formatted(filename));
        }

        return observations;
    }

    private LevelingObservation parseLine(String[] columns, int lineNumber) throws ParsingException {
        try {
            boolean isDouble = columns.length == COLUMNS_ONE_WAY_DOUBLE;

            return new LevelingObservation(
                    columns[0],
                    columns[1],
                    columns[2],
                    Integer.parseInt(columns[3]),
                    Integer.parseInt(columns[4]),
                    Double.parseDouble(columns[5]),
                    Double.parseDouble(columns[6]),
                    isDouble ? Double.parseDouble(columns[7]) : null,
                    isDouble ? Double.parseDouble(columns[8]) : null
            );

        } catch (NumberFormatException e) {
            throw new ParsingException(
                    "Failed to parse numeric value at line [%s]: [%s]".formatted(lineNumber, e.getMessage())
            );
        }
    }

    @Override
    public boolean supports(String filename) {
        return filename.endsWith(".txt");
    }
}
