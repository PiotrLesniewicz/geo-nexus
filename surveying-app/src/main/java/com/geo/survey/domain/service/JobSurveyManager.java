package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.model.*;
import com.geo.survey.math.value.LevelingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            Long jobId,
            Double startH,
            Double endH,
            InputStream stream,
            String filename,
            LevelingType type,
            OffsetDateTime observationTime
    ) {
        Job job = jobService.getById(jobId);
        if (job.getStatus() != StatusJob.OPEN) {
            throw new BusinessRuleViolationException("Job is not open");
        }
        return levelingService.processFile(stream, startH, endH, filename, type, job, observationTime);
    }

    public List<LevelingReport> findReportForJobId(Long jobId) {
        return levelingService.findReportsByJobId(jobId);
    }
}
