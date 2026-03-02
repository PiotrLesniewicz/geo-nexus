package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByNip(String nip);

    Optional<CompanyEntity> findByNip(String nip);
}
