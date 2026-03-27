package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.ParsingException;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.infrastructure.database.entity.LevelingReportEntity;
import com.geo.survey.infrastructure.database.repository.LevelingReportRepository;
import com.geo.survey.infrastructure.mapper.LevelingReportMapper;
import com.geo.survey.infrastructure.mapper.LevelingResultMapper;
import com.geo.survey.math.engine.LevelingEngine;
import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingResultReport;
import com.geo.survey.math.value.LevelingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelingService {

    private final List<LevelingFileParser> parsers;
    private final ValidationService validationService;
    private final LevelingStrategy levelingStrategy;
    private final LevelingResultMapper levelingResultMapper;
    private final LevelingReportMapper levelingReportMapper;
    private final LevelingReportRepository levelingReportRepository;
    private final Clock clock;

    public LevelingReport processFile(
            Job job,
            Double startH,
            Double endH,
            InputStream stream,
            String filename,
            LevelingType type,
            OffsetDateTime observationTime
    ) {
        List<LevelingObservation> observations = getParser(filename).parse(stream, filename);

        validationService.validate(observations, type);

        LevelingEngine engine = levelingStrategy.getEngine(type);
        LevelingResultReport calculationResult = engine.calculate(startH, endH, observations);

        LevelingReport report = levelingResultMapper.toDomain(calculationResult);
        LevelingReport toSave = LevelingReport.generated(report, type, job, observationTime, clock);
        LevelingReportEntity saved = levelingReportRepository.save(levelingReportMapper.toEntity(toSave));

        return levelingReportMapper.toDomain(saved);
    }


    private LevelingFileParser getParser(String filename) {
        return parsers.stream()
                .filter(p -> p.supports(filename))
                .findFirst()
                .orElseThrow(() -> new ParsingException("Unsupported file format: " + filename));
    }

    public List<LevelingReport> findLevelingReports(String jobIdentifier) {
        List<LevelingReportEntity> jobEntity = levelingReportRepository.findByJobIdentifier(jobIdentifier);
        return jobEntity.stream()
                .map(levelingReportMapper::toDomain)
                .toList();
    }
}
