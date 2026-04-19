package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.JobListItem;
import com.geo.survey.infrastructure.database.entity.JobEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UserMapper.class, CompanyMapper.class, AddressMapper.class})
public interface JobMapper {
    JobEntity toEntity(Job domain);

    Job toDomain(JobEntity entity);

    @Mapping(source = "address.city", target = "city")
    JobListItem toListItem(JobEntity entity);
}
