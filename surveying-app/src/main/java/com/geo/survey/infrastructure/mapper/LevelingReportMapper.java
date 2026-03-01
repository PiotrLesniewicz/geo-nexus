package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.infrastructure.database.entity.LevelingReportEntity;
import org.mapstruct.Mapper;

@Mapper(uses = {LevelingStationMapper.class})
public interface LevelingReportMapper {
    LevelingReportEntity toEntity(LevelingReport domain);

    LevelingReport toDomain(LevelingReportEntity entity);
}
