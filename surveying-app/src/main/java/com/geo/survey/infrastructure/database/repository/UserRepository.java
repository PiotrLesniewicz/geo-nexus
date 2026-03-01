package com.geo.survey.infrastructure.database.repository;

import com.geo.survey.infrastructure.database.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
