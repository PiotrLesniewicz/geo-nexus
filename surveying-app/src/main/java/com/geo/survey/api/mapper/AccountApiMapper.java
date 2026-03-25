package com.geo.survey.api.mapper;

import com.geo.survey.api.dto.RegisterCompanyRequest;
import com.geo.survey.api.dto.RegisterUserRequest;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface AccountApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "registerAt", ignore = true)
    @Mapping(target = "blockedAt", ignore = true)
    @Mapping(target = "name", source = "companyName")
    @Mapping(target = "address.street", source = "street")
    @Mapping(target = "address.buildingNumber", source = "buildingNumber")
    @Mapping(target = "address.apartmentNumber", source = "apartmentNumber")
    @Mapping(target = "address.postalCode", source = "postalCode")
    @Mapping(target = "address.city", source = "city")
    @Mapping(target = "address.county", source = "county")
    @Mapping(target = "address.voivodeship", source = "voivodeship")
    @Mapping(target = "address.country", source = "country")
    Company toCompany(RegisterCompanyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "registerAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "email", source = "adminEmail")
    @Mapping(target = "name", source = "adminName")
    @Mapping(target = "surname", source = "adminSurname")
    @Mapping(target = "role", ignore = true)
    User toAdminUser(RegisterCompanyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "registerAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    User toUser(RegisterUserRequest request);
}
