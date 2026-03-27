package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ParsingException;
import com.geo.survey.domain.model.*;
import com.geo.survey.math.value.LevelingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobSurveyManager {

    private final CompanyService companyService;
    private final UserService userService;
    private final JobService jobService;
    private final LevelingService levelingService;

    @Transactional
    public Job createJob(Job job, Long companyId, Long userId) {
        Company company = companyService.findById(companyId);
        if (!company.isActive()) {
            throw new BusinessRuleViolationException("Company is not active");
        }
        User user = userService.findById(userId);
        if (!user.isActive()) {
            throw new BusinessRuleViolationException("User is not active");
        }
        return jobService.create(job, company, user);
    }

    @Transactional
    public LevelingReport processLevelingFile(
            String jobIdentifier,
            Double startH,
            Double endH,
            MultipartFile file,
            LevelingType type,
            OffsetDateTime observationTime
    ) {
        Job job = jobService.getByJobIdentifier(jobIdentifier);
        if (job.getStatus() != StatusJob.OPEN) {
            throw new BusinessRuleViolationException("Job is not open");
        }
        try (InputStream stream = file.getInputStream()) {
            return levelingService.processFile(job, startH, endH, stream, file.getOriginalFilename(), type, observationTime);
        } catch (IOException e) {
            throw new ParsingException("Failed to read file: [%s], [%s]".formatted(file.getOriginalFilename(), e));
        }
    }

    public Job findJobByIdentifier(String jobIdentifier) {
        return jobService.getByJobIdentifier(jobIdentifier);
    }

    public List<LevelingReport> findLevelingReports(String jobIdentifier) {
        return levelingService.findLevelingReports(jobIdentifier);
    }
}
