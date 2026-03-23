package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.LevelingReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LevelingReportRepository extends JpaRepository<LevelingReportEntity, Long> {
    List<LevelingReportEntity> findByJobId(Long jobId);
}
