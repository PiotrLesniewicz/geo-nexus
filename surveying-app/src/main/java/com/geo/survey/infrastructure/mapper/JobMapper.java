package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.Job;
import com.geo.survey.infrastructure.database.entity.JobEntity;
import org.mapstruct.Mapper;

@Mapper
public interface JobMapper {
    JobEntity toEntity(Job domain);

    Job toDomain(JobEntity entity);
}
