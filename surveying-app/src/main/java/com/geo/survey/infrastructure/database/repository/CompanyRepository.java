package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
}
