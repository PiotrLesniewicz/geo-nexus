package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
    boolean existsByJobIdentifier(String jobIdentifier);

    Page<JobEntity> findAllByCompanyId(Long companyId, Pageable pageable);

    Page<JobEntity> findAllByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"company", "user"})
    Optional<JobEntity> findByJobIdentifierAndCompanyId(String jobIdentifier, Long companyId);

    int countByUserId(Long userId);

    int countOpenByUserId(Long userId);
}
