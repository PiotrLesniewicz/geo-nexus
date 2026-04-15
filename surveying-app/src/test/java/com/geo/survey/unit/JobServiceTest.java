package com.geo.survey.unit;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.StatusJob;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.service.JobService;
import com.geo.survey.infrastructure.database.entity.JobEntity;
import com.geo.survey.infrastructure.database.repository.JobRepository;
import com.geo.survey.infrastructure.mapper.*;
import com.geo.survey.testdata.CompanyFixture;
import com.geo.survey.testdata.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Optional;

import static com.geo.survey.testdata.JobFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    private JobService jobService;

    @Mock
    private JobRepository jobRepository;
    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        AddressMapper addressMapper = new AddressMapperImpl();
        CompanyMapper companyMapper = new CompanyMapperImpl(addressMapper);
        UserMapper userMapper = new UserMapperImpl();
        JobMapper jobMapper = new JobMapperImpl(userMapper, companyMapper, addressMapper);
        jobService = new JobService(jobRepository, jobMapper, clock);
    }

    // get by id tests

    @Test
    void shouldThrowException_WhenJobNotFoundByJobIdentifier() {
        // given
        Long companyId = 1L;
        String jobIdentifier = "JOB.2025.10";

        when(jobRepository.findByJobIdentifierAndCompanyId(jobIdentifier, companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> jobService.getByJobIdentifier(jobIdentifier, companyId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found")
                .hasMessageContaining(jobIdentifier);

        verify(jobRepository).findByJobIdentifierAndCompanyId(jobIdentifier, companyId);
    }

    // create tests

    @Test
    void shouldCorrectlyCreateJob_WhenJobIdentifierDoesNotExist() {
        // given
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(java.time.ZoneOffset.UTC);

        Job jobToCreate = jobWithoutStatus();
        Company company = CompanyFixture.activeCompanyWithId();
        User user = UserFixture.activeUser();

        when(jobRepository.existsByJobIdentifier(DEFAULT_JOB_IDENTIFIER)).thenReturn(false);
        when(jobRepository.save(any(JobEntity.class)))
                .thenAnswer(i -> {
                    JobEntity entity = i.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        // when
        Job result = jobService.create(jobToCreate, company, user);

        // then
        assertThat(result)
                .isNotNull()
                .extracting(
                        Job::getId,
                        Job::getJobIdentifier,
                        Job::getStatus,
                        Job::getDescription
                )
                .containsExactly(
                        1L,
                        DEFAULT_JOB_IDENTIFIER,
                        StatusJob.OPEN,
                        DEFAULT_DESCRIPTION
                );

        assertThat(result.getCreatedAt()).isEqualTo(fixedDateTime());
        assertThat(result.getCompany().getId()).isEqualTo(company.getId());
        assertThat(result.getUser().getEmail()).isEqualTo(user.getEmail());

        verify(jobRepository).existsByJobIdentifier(DEFAULT_JOB_IDENTIFIER);
        verify(jobRepository).save(any(JobEntity.class));
    }

    @Test
    void shouldThrowException_WhenJobIdentifierAlreadyExists() {
        // given
        Job jobToCreate = jobWithoutStatus();
        Company company = CompanyFixture.activeCompanyWithId();
        User user = UserFixture.activeUser();

        when(jobRepository.existsByJobIdentifier(DEFAULT_JOB_IDENTIFIER)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> jobService.create(jobToCreate, company, user))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Job already exists")
                .hasMessageContaining(DEFAULT_JOB_IDENTIFIER);

        verify(jobRepository).existsByJobIdentifier(DEFAULT_JOB_IDENTIFIER);
        verify(jobRepository, never()).save(any());
    }

    // delete tests

    @Test
    void shouldThrowException_WhenDeletingNonExistentJob() {
        // given
        Long jobId = 999L;

        when(jobRepository.existsById(jobId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jobService.delete(jobId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found")
                .hasMessageContaining(jobId.toString());

        verify(jobRepository).existsById(jobId);
        verify(jobRepository, never()).deleteById(anyLong());
    }

}

