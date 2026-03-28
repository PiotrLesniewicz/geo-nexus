package com.geo.survey.api.mapper;

import com.geo.survey.api.dto.CreateJobRequest;
import com.geo.survey.api.dto.JobResponse;
import com.geo.survey.api.dto.LevelingReportResponse;
import com.geo.survey.api.dto.LevelingStationResponse;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.domain.model.LevelingStation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface JobApiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address.street", source = "street")
    @Mapping(target = "address.buildingNumber", source = "buildingNumber")
    @Mapping(target = "address.apartmentNumber", source = "apartmentNumber")
    @Mapping(target = "address.postalCode", source = "postalCode")
    @Mapping(target = "address.city", source = "city")
    @Mapping(target = "address.county", source = "county")
    @Mapping(target = "address.voivodeship", source = "voivodeship")
    @Mapping(target = "address.country", source = "country")
    Job toJob(CreateJobRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "buildingNumber", source = "address.buildingNumber")
    @Mapping(target = "apartmentNumber", source = "address.apartmentNumber")
    @Mapping(target = "postalCode", source = "address.postalCode")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "county", source = "address.county")
    @Mapping(target = "voivodeship", source = "address.voivodeship")
    @Mapping(target = "country", source = "address.country")
    JobResponse toJobResponse(Job job);

    @Mapping(target = "jobIdentifier", source = "job.jobIdentifier")
    LevelingReportResponse toLevelingReportResponse(LevelingReport report);

    List<LevelingReportResponse> toLevelingReportResponseList(List<LevelingReport> reports);

    LevelingStationResponse toLevelingStationResponse(LevelingStation station);
}
