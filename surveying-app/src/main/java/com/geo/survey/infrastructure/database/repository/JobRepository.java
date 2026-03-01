package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
}
