package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
    boolean existsByJobIdentifier(String jobIdentifier);

    List<JobEntity> findAllByCompanyId(Long companyId);

    List<JobEntity> findAllByUserId(Long userId);

    Optional<JobEntity> findByJobIdentifier(String jobIdentifier);
}
