package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ParsingException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.exception.UnauthorizedAccessException;
import com.geo.survey.domain.model.*;
import com.geo.survey.math.value.LevelingType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class JobSurveyManager {

    private final CompanyService companyService;
    private final UserService userService;
    private final UserAuthService userAuthService;
    private final JobService jobService;
    private final LevelingService levelingService;

    @Transactional
    public Job createJob(Job job, Long companyId, Long userId) {
        Company company = companyService.findById(companyId);
        if (!company.isActive()) {
            throw new BusinessRuleViolationException("Company is not active");
        }
        User user = userService.findById(userId);
        return jobService.create(job, company, user);
    }

    @Transactional
    public LevelingReport processLevelingFile(
            Long companyId,
            String jobIdentifier,
            Double startH,
            Double endH,
            MultipartFile file,
            LevelingType type,
            OffsetDateTime observationTime
    ) {
        Job job = jobService.getByJobIdentifier(jobIdentifier, companyId);
        if (job.getStatus() != StatusJob.OPEN) {
            throw new BusinessRuleViolationException("Job is not open");
        }
        try (InputStream stream = file.getInputStream()) {
            return levelingService.processFile(job, startH, endH, stream, file.getOriginalFilename(), type, observationTime);
        } catch (IOException e) {
            throw new ParsingException("Failed to read file: [%s], [%s]".formatted(file.getOriginalFilename(), e));
        }
    }

    public Job findJobByIdentifier(Long companyId, String jobIdentifier) {
        return jobService.getByJobIdentifier(jobIdentifier, companyId);
    }

    public Page<LevelingReport> findLevelingReports(String jobIdentifier, Long companyId, Pageable pageable) throws BusinessRuleViolationException {
        if (!jobService.existsByJobIdentifierAndCompanyId(jobIdentifier, companyId)) {
            throw new ResourceNotFoundException("Job with identifier [%s] does not exist".formatted(jobIdentifier));
        }
        return levelingService.findLevelingReports(jobIdentifier, companyId, pageable);
    }

    public Page<JobListItem> getAllJobsForCompany(Long companyId, Pageable pageable) {
        return jobService.getAllForCompany(companyId, pageable);
    }

    public Page<JobListItem> getAllJobsForUser(Long userId, Pageable pageable) {
        return jobService.getAllForUser(userId, pageable);
    }

    @Transactional
    public void delete(Long companyId, Long userId, String password, String jobIdentifier) {
        if (!userAuthService.verifyPassword(userId, password)) {
            throw new UnauthorizedAccessException("Incorrect current password");
        }
        jobService.delete(jobIdentifier, companyId);
    }

    @Transactional
    public void closeJob(String jobIdentifier, Long userId) {
        Job job = jobService.getByJobIdentifier(jobIdentifier, userId);
        if (job.getStatus() != StatusJob.OPEN) {
            throw new BusinessRuleViolationException("Only open jobs can be closed");
        }
        jobService.closeJob(job);
    }

    @Transactional
    public void openJob(String jobIdentifier, Long userId) {
        Job job = jobService.getByJobIdentifier(jobIdentifier, userId);
        if (job.getStatus() != StatusJob.CLOSED) {
            throw new BusinessRuleViolationException("Only close jobs can be opened");
        }
        jobService.openJob(job);
    }
}
