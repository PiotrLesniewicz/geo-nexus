package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.Company;
import com.geo.survey.infrastructure.database.entity.CompanyEntity;
import org.mapstruct.Mapper;

@Mapper(uses = {AddressMapper.class})
public interface CompanyMapper {
    CompanyEntity toEntity(Company domain);

    Company toDomain(CompanyEntity entity);
}
