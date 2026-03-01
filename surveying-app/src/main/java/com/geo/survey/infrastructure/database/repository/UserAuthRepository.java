package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.UserAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthRepository extends JpaRepository<UserAuthEntity, Long> {
}