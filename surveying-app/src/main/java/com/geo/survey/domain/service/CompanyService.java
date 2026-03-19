package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Company;
import com.geo.survey.infrastructure.database.entity.CompanyEntity;
import com.geo.survey.infrastructure.database.repository.CompanyRepository;
import com.geo.survey.infrastructure.mapper.CompanyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper mapper;
    private final Clock clock;

    public Company save(Company company) {
        validateNipUniqueness(company.getNip());
        Company register = Company.register(company.getName(), company.getNip(), company.getAddress(), clock);
        CompanyEntity entity = mapper.toEntity(register);
        CompanyEntity savedEntity = companyRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    public Company blockCompany(Company company) {
        Company blocked = company.block(clock);
        CompanyEntity entity = mapper.toEntity(blocked);
        CompanyEntity saved = companyRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Company activateCompany(Company company) {
        Company activated = company.activate();
        CompanyEntity entity = mapper.toEntity(activated);
        CompanyEntity saved = companyRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Company findByNip(String nip) {
        return companyRepository.findByNip(nip)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company with nip [%s] does not exist".formatted(nip))
                );
    }

    public Company findById(Long companyId) {
        return companyRepository.findById(companyId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company with companyId [%s] does not exist".formatted(companyId))
                );
    }

    private void validateNipUniqueness(String nip) {
        if (companyRepository.existsByNip(nip)) {
            throw new BusinessRuleViolationException("Company with nip [%s] already exists".formatted(nip));
        }
    }
}
