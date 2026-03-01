package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.User;
import com.geo.survey.infrastructure.database.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserEntity toEntity(User domain);

    User toDomain(UserEntity entity);
}
