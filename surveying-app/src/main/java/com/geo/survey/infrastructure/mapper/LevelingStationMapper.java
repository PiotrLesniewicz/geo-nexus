package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.LevelingStation;
import com.geo.survey.infrastructure.database.entity.LevelingStationSnapshot;
import org.mapstruct.Mapper;

@Mapper
public interface LevelingStationMapper {
    LevelingStationSnapshot toEntity(LevelingStation domain);

    LevelingStation toDomain(LevelingStationSnapshot entity);
}
