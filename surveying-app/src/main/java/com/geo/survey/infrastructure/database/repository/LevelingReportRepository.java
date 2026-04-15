package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.LevelingReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LevelingReportRepository extends JpaRepository<LevelingReportEntity, Long> {
    @Query("""
            SELECT lr FROM LevelingReportEntity lr
            JOIN lr.job j WHERE j.jobIdentifier = :jobIdentifier
                        AND j.company.id = :companyId
            """)
    Page<LevelingReportEntity> findByJobIdentifierAndCompanyId(
            @Param("jobIdentifier") String jobIdentifier,
            @Param("companyId") Long companyId,
            Pageable pageable
    );
}
