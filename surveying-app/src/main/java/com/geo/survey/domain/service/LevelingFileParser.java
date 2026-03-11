package com.geo.survey.domain.service;

import com.geo.survey.math.value.LevelingObservation;
import org.hibernate.query.sqm.ParsingException;

import java.io.InputStream;
import java.util.List;

public interface LevelingFileParser {
    List<LevelingObservation> parse(InputStream inputStream, String filename) throws ParsingException;

    boolean supports(String filename);
}
