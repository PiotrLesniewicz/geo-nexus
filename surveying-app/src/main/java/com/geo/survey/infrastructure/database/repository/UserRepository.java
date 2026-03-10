package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.company.id = :companyId AND u.role = 'ADMIN' AND u.active = true")
    long countActiveAdminsByCompanyId(@Param("companyId") Long companyId);
}
