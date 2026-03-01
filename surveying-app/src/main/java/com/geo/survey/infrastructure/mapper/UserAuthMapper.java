package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.UserAuth;
import com.geo.survey.infrastructure.database.entity.UserAuthEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UserAuthMapper {
    UserAuthEntity toEntity(UserAuth domain);

    UserAuth toDomain(UserAuthEntity entity);
}
