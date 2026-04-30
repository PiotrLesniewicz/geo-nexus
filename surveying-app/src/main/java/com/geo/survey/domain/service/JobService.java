package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.JobListItem;
import com.geo.survey.domain.model.User;
import com.geo.survey.infrastructure.database.entity.JobEntity;
import com.geo.survey.infrastructure.database.repository.JobRepository;
import com.geo.survey.infrastructure.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final Clock clock;

    public Job create(Job job, Company company, User user) {
        if (jobRepository.existsByJobIdentifier(job.getJobIdentifier())) {
            throw new BusinessRuleViolationException(
                    "Job already exists with identifier: [%s]".formatted(job.getJobIdentifier()));
        }
        Job toSave = Job.create(job, company, user, clock);
        JobEntity entity = jobMapper.toEntity(toSave);
        return jobMapper.toDomain(jobRepository.save(entity));
    }

    public Job getByJobIdentifier(String jobIdentifier, Long companyId) {
        return jobRepository.findByJobIdentifierAndCompanyId(jobIdentifier, companyId)
                .map(jobMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with jobIdentifier: [%s]".formatted(jobIdentifier)));
    }

    public Page<JobListItem> getAllForCompany(Long companyId, Pageable pageable) {
        return jobRepository.findAllByCompanyId(companyId, pageable)
                .map(jobMapper::toListItem);
    }

    public Page<JobListItem> getAllForUser(Long userId, Pageable pageable) {
        return jobRepository.findAllByUserId(userId, pageable)
                .map(jobMapper::toListItem);
    }

    public void delete(String jobIdentifier, Long companyId) {
        int deleted = jobRepository.deleteByJobIdentifierAndCompanyId(jobIdentifier, companyId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Job not found with id: [%s]".formatted(jobIdentifier));
        }
    }

    public int countByUserId(Long userId) {
        return jobRepository.countByUserId(userId);
    }

    public int countOpenByUserId(Long userId) {
        return jobRepository.countOpenByUserId(userId);
    }

    public void closeJob(Job job) {
        JobEntity entity = jobMapper.toEntity(job.close());
        jobRepository.save(entity);
    }

    public void openJob(Job job) {
        JobEntity entity = jobMapper.toEntity(job.open());
        jobRepository.save(entity);
    }
}
