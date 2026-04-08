package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.UserAuthEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, Long> {
    Optional<UserAuthEntity> findByUserId(Long id);

    @EntityGraph(attributePaths = {"user", "user.company"})
    Optional<UserAuthEntity> findByUserEmail(String email);
}