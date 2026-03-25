package com.geo.survey.unit;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.service.CompanyService;
import com.geo.survey.infrastructure.database.entity.CompanyEntity;
import com.geo.survey.infrastructure.database.repository.CompanyRepository;
import com.geo.survey.infrastructure.mapper.AddressMapper;
import com.geo.survey.infrastructure.mapper.AddressMapperImpl;
import com.geo.survey.infrastructure.mapper.CompanyMapper;
import com.geo.survey.infrastructure.mapper.CompanyMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.geo.survey.testdata.CompanyFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    private CompanyService companyService;

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        AddressMapper addressMapper = new AddressMapperImpl();
        CompanyMapper companyMapper = new CompanyMapperImpl(addressMapper);
        companyService = new CompanyService(companyRepository, companyMapper, clock);
    }

    @Test
    void shouldCorrectlySaveCompany_WhenCompanyDoesNotExist() {
        // given
        mockClock();
        Company company = companyWithAddress();

        when(companyRepository.existsByNip(DEFAULT_NIP)).thenReturn(false);
        when(companyRepository.save(any(CompanyEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // when
        Company result = companyService.save(company);

        // then
        assertNotNull(result);
        assertEquals(DEFAULT_NIP, result.getNip());
        assertEquals(fixedDateTime(), result.getRegisterAt());
        assertThat(result.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(result.isActive()).isTrue();

        verify(companyRepository).existsByNip(DEFAULT_NIP);
        verify(companyRepository).save(any(CompanyEntity.class));
    }

    @Test
    void shouldThrowException_WhenNipAlreadyExists() {
        // given
        Company company = companyWithoutStatus();

        when(companyRepository.existsByNip(DEFAULT_NIP)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> companyService.save(company))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining(DEFAULT_NIP);

        verify(companyRepository).existsByNip(DEFAULT_NIP);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_WhenCompanyNotFoundByNip() {
        // given
        when(companyRepository.findByNip(DEFAULT_NIP)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyService.findByNip(DEFAULT_NIP))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(DEFAULT_NIP);

        verify(companyRepository).findByNip(DEFAULT_NIP);
    }

    @Test
    void shouldBlockActiveCompany() {
        // given
        mockClock();
        when(companyRepository.save(any(CompanyEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // when
        Company result = companyService.blockCompany(activeCompany());

        // then
        assertThat(result.isActive()).isFalse();
        assertThat(result.getBlockedAt()).isEqualTo(fixedDateTime());
        verify(companyRepository).save(any(CompanyEntity.class));
    }

    @Test
    void shouldThrowException_WhenBlockingAlreadyBlockedCompany() {
        // given
        Company company = blockedCompany();
        // when & then
        assertThatThrownBy(() -> companyService.blockCompany(company))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldActivateBlockedCompany() {
        // given
        when(companyRepository.save(any(CompanyEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        // when
        Company result = companyService.activateCompany(blockedCompany());

        // then
        assertThat(result.isActive()).isTrue();
        assertThat(result.getBlockedAt()).isNull();
        verify(companyRepository).save(any(CompanyEntity.class));
    }

    @Test
    void shouldThrowException_WhenActivatingAlreadyActiveCompany() {
        // given
        Company company = activeCompany();

        //when, then
        assertThatThrownBy(() -> companyService.activateCompany(company))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(companyRepository, never()).save(any());
    }

    private void mockClock() {
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }
}