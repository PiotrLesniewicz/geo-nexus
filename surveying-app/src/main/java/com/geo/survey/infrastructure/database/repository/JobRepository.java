package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
    boolean existsByJobIdentifier(String jobIdentifier);

    List<JobEntity> findAllByCompany_Id(Long companyId);

    List<JobEntity> findAllByUser_Id(Long userId);
}
