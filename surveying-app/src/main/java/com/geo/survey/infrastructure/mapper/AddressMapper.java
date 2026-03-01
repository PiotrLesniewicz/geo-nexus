package com.geo.survey.infrastructure.mapper;

import com.geo.survey.domain.model.Address;
import com.geo.survey.infrastructure.database.entity.AddressEntity;
import org.mapstruct.Mapper;

@Mapper
public interface AddressMapper {
    AddressEntity toEntity(Address domain);

    Address toDomain(AddressEntity entity);
}
