package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.LevelingReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LevelingReportRepository extends JpaRepository<LevelingReportEntity, Long> {
    @Query("""
            SELECT lr FROM LevelingReportEntity lr
            JOIN lr.job j WHERE j.jobIdentifier = :jobIdentifier
            """)
    List<LevelingReportEntity> findByJobIdentifier(@Param("jobIdentifier") String jobIdentifier);
}
