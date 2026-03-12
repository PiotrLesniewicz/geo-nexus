package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.ResourceAlreadyExistsException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.User;
import com.geo.survey.infrastructure.database.entity.JobEntity;
import com.geo.survey.infrastructure.database.repository.JobRepository;
import com.geo.survey.infrastructure.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final Clock clock;

    public Job getById(Long id) {
        return jobRepository.findById(id)
                .map(jobMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: [%d]".formatted(id)));
    }

    public List<Job> getAllByCompany(Long companyId) {
        return jobRepository.findAllByCompany_Id(companyId)
                .stream()
                .map(jobMapper::toDomain)
                .toList();
    }

    public List<Job> getAllByUser(Long userId) {
        return jobRepository.findAllByUser_Id(userId)
                .stream()
                .map(jobMapper::toDomain)
                .toList();
    }

    public Job create(Job job, Company company, User user) {
        if (jobRepository.existsByJobIdentifier(job.getJobIdentifier())) {
            throw new ResourceAlreadyExistsException(
                    "Job already exists with identifier: [%s]".formatted(job.getJobIdentifier()));
        }
        Job toSave = Job.create(job, company, user, clock);
        JobEntity entity = jobMapper.toEntity(toSave);
        return jobMapper.toDomain(jobRepository.save(entity));
    }

    public void delete(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Job not found with id: [%d]".formatted(id));
        }
        jobRepository.deleteById(id);
    }
}
