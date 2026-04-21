package com.geo.survey.integration;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.*;
import com.geo.survey.domain.service.JobSurveyManager;
import com.geo.survey.math.value.LevelingType;
import com.geo.survey.testconfig.TestContainerConfig;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AllArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/db/migration/cleanup.sql", "/db/migration/test_data_job_leveling.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JobSurveyManagerIntegrationTest extends TestContainerConfig {

    private JobSurveyManager jobSurveyManager;

    @MockitoBean
    private Clock clock;

    // data user, company and job from test_data_job_leveling.sql
    private static final String JOB_IDENTIFIER = "JOB-2024-001";
    private static final Long COMPANY_ID = 1L;
    private static final Long USER_ID_1 = 1L;

    private static final Double START_H = 100.000;
    private static final Double END_H = 100.500;

    @BeforeEach
    void setUp() {
        Instant fixInstant = Instant.parse("2024-06-15T10:00:00Z");
        Mockito.when(clock.instant()).thenReturn(fixInstant);
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    // createJob tests

    @Test
    void shouldCreateJob_withCompanyAndUser() {
        // given
        Job job = Job.builder()
                .jobIdentifier("JOB-2024-NEW")
                .description("New test measurement")
                .build();

        // when
        Job result = jobSurveyManager.createJob(job, COMPANY_ID, USER_ID_1);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getJobIdentifier()).isEqualTo("JOB-2024-NEW");
        assertThat(result.getCompany().getId()).isEqualTo(COMPANY_ID);
        assertThat(result.getUser().getId()).isEqualTo(USER_ID_1);
        assertThat(result.getStatus()).isEqualTo(StatusJob.OPEN);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    // ONE_WAY leveling tests

    @Test
    void shouldProcessOneWayLevelingFile_andSaveReportToDatabase() throws IOException {
        // given
        InputStream stream = getStream("leveling/one_way_10stations.csv");
        MultipartFile uploadFile = new MockMultipartFile("file", "one_way_10stations.csv", null, stream);
        OffsetDateTime observationTime = OffsetDateTime.now(clock);
        BigDecimal expectedMisclosure = new BigDecimal("0.0020"); // calculated manually
        BigDecimal expectedSequenceDistance = new BigDecimal("0.4860");

        // when
        LevelingReport result = reportOneWayLeveling(uploadFile, observationTime);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getLevelingType()).isEqualTo(LevelingType.ONE_WAY);
        assertThat(result.getMisclosure()).isEqualByComparingTo(expectedMisclosure);
        assertThat(result.getSequenceDistance()).isEqualByComparingTo(expectedSequenceDistance);

        assertThat(result.getJob().getJobIdentifier()).isEqualTo(JOB_IDENTIFIER);
        assertThat(result.getJob().getCompany().getId()).isEqualTo(COMPANY_ID);
        assertThat(result.getStations())
                .isNotNull()
                .hasSize(10);

        LevelingStation firstStation = result.getStations().getFirst();
        assertThat(firstStation.getAdjustedHeight()).isNotNull();
        assertThat(firstStation.getCorrection()).isNotNull();
        assertThat(firstStation.getBacksightElev2()).isNull();
        assertThat(firstStation.getForesightElev2()).isNull();
    }

    @Test
    void shouldProcessOneWayLevelingFile_withoutStartAndEndH() throws IOException {
        InputStream stream = getStream("leveling/one_way_10stations.csv");
        MultipartFile uploadFile = new MockMultipartFile("file", "one_way_10stations.csv", null, stream);
        OffsetDateTime observationTime = OffsetDateTime.now(clock);

        // when
        LevelingReport result = jobSurveyManager.processLevelingFile(
                COMPANY_ID,
                JOB_IDENTIFIER,
                null,
                null,
                uploadFile,
                LevelingType.ONE_WAY,
                observationTime
        );

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStartHeight()).isNull();
        assertThat(result.getEndHeight()).isNull();
        assertThat(result.getStations()).hasSize(10);
    }

    // ONE_WAY_DOUBLE leveling tests

    @Test
    void shouldProcessOneWayDoubleLevelingFile_andSaveReportToDatabase() throws IOException {
        // given
        InputStream stream = getStream("leveling/one_way_double_10stations.csv");
        MultipartFile uploadFile = new MockMultipartFile("file", "one_way_double_10stations.csv", null, stream);
        OffsetDateTime observationTime = OffsetDateTime.now(clock);
        BigDecimal expectedMisclosure = new BigDecimal("-0.0025"); // calculated manually
        BigDecimal expectedSequenceDistance = new BigDecimal("0.4860");

        // when
        LevelingReport result = reportOneWayDoubleLeveling(uploadFile, observationTime);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getLevelingType()).isEqualTo(LevelingType.ONE_WAY_DOUBLE);
        assertThat(result.getMisclosure()).isEqualByComparingTo(expectedMisclosure);
        assertThat(result.getSequenceDistance()).isEqualByComparingTo(expectedSequenceDistance);


        assertThat(result.getLevelingType()).isEqualTo(LevelingType.ONE_WAY_DOUBLE);
        assertThat(result.getStations()).hasSize(10);

        LevelingStation firstStation = result.getStations().getFirst();
        assertThat(firstStation.getBacksightElev2()).isNotNull();
        assertThat(firstStation.getForesightElev2()).isNotNull();
        assertThat(firstStation.getHeightDiffSecond()).isNotNull();
        assertThat(firstStation.getStationError()).isNotNull();
        assertThat(firstStation.isToleranceMet()).isTrue();
    }

    // add two report for one job

    @Test
    void shouldStoreTwoLevelingReports_andReturnBothForSameJob() throws IOException {
        // given
        InputStream streamFirst = getStream("leveling/one_way_10stations.csv");
        InputStream streamSecond = getStream("leveling/one_way_double_10stations.csv");
        MultipartFile uploadFileFirst = new MockMultipartFile("file", "one_way_10stations.csv", null, streamFirst);
        MultipartFile uploadFileSecond = new MockMultipartFile("file", "one_way_double_10stations.csv", null, streamSecond);
        OffsetDateTime observationTime = OffsetDateTime.now(clock);

        BigDecimal expectedMisclosureFirst = new BigDecimal("0.0020");
        BigDecimal expectedMisclosureSecond = new BigDecimal("-0.0025");

        Pageable pageable = PageRequest.of(0, 10);

        // when
        LevelingReport firstReport = reportOneWayLeveling(uploadFileFirst, observationTime);

        LevelingReport secondReport = reportOneWayDoubleLeveling(uploadFileSecond, observationTime);

        // then
        assertThat(firstReport.getId()).isNotNull();
        assertThat(secondReport.getId()).isNotNull();
        assertThat(firstReport.getId()).isNotEqualTo(secondReport.getId());


        assertThat(firstReport.getJob().getJobIdentifier()).isEqualTo(JOB_IDENTIFIER);
        assertThat(secondReport.getJob().getJobIdentifier()).isEqualTo(JOB_IDENTIFIER);


        assertThat(firstReport.getLevelingType()).isEqualTo(LevelingType.ONE_WAY);
        assertThat(firstReport.getMisclosure()).isEqualByComparingTo(expectedMisclosureFirst);
        assertThat(firstReport.getStations()).hasSize(10);

        assertThat(secondReport.getLevelingType()).isEqualTo(LevelingType.ONE_WAY_DOUBLE);
        assertThat(secondReport.getMisclosure()).isEqualByComparingTo(expectedMisclosureSecond);
        assertThat(secondReport.getStations()).hasSize(10);


        assertThat(firstReport.getStations().getFirst().getBacksightElev2()).isNull();
        assertThat(secondReport.getStations().getFirst().getBacksightElev2()).isNotNull();

        Page<LevelingReport> reportsForJob = jobSurveyManager.findLevelingReports(JOB_IDENTIFIER, COMPANY_ID, pageable);
        assertThat(reportsForJob.getTotalElements()).isEqualTo(2);
    }

    // createJob business rule validation tests

    @Test
    void shouldThrowException_WhenCreatingJobWithInactiveCompany() {
        // given
        Long inactiveCompanyId = 3L; // TerraMap Biuro Geodezji - active = FALSE from test_data_job_leveling.sql

        Job job = Job.builder()
                .jobIdentifier("JOB-2024-INACTIVE-COMPANY")
                .description("Job with inactive company")
                .build();

        // when & then
        assertThatThrownBy(() -> jobSurveyManager.createJob(job, inactiveCompanyId, USER_ID_1))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Company is not active");
    }

    // processLevelingFile business rule validation tests

    @Test
    void shouldThrowException_WhenProcessingLevelingFileForClosedJob() throws IOException {
        // given
        String closedJobIdentifier = "JOB-2024-CLOSED"; // closed job from test_data_job_leveling.sql

        InputStream stream = getStream("leveling/one_way_10stations.csv");
        MultipartFile uploadFile = new MockMultipartFile("one_way_10stations.csv", stream);
        OffsetDateTime observationTime = OffsetDateTime.now(clock);

        // when & then
        assertThatThrownBy(() -> jobSurveyManager.processLevelingFile(
                COMPANY_ID,
                closedJobIdentifier,
                START_H,
                END_H,
                uploadFile,
                LevelingType.ONE_WAY,
                observationTime
        ))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Job is not open");
    }

    // get job summary

    @Test
    void shouldReturnJobSummaryForCompany() {
        // given
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<JobListItem> result = jobSurveyManager.getAllJobsForCompany(COMPANY_ID, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(7);
        assertThat(result.getTotalPages()).isEqualTo(2);

        assertThat(result.getContent())
                .hasSize(5)
                .doesNotContainNull()
                .allSatisfy(item -> {
                    assertThat(item.getJobIdentifier()).isNotNull();
                    assertThat(item.getCity()).isNotNull();
                    assertThat(item.getStatus()).isNotNull();
                    assertThat(item.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void shouldReturnJobSummaryForUser() {
        // given
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<JobListItem> result = jobSurveyManager.getAllJobsForUser(USER_ID_1, pageable);

        // then

        assertThat(result.getTotalElements()).isEqualTo(7);
        assertThat(result.getTotalPages()).isEqualTo(2);

        assertThat(result.getContent())
                .hasSize(5)
                .doesNotContainNull()
                .allSatisfy(item -> {
                    assertThat(item.getJobIdentifier()).isNotNull();
                    assertThat(item.getCity()).isNotNull();
                    assertThat(item.getStatus()).isNotNull();
                    assertThat(item.getCreatedAt()).isNotNull();
                });
    }

    // delete job

    @Test
    void shouldCorrectlyDeleteJob() {
        // given
        String password = "Password123"; // from test_data_job_leveling.sql

        // when
        jobSurveyManager.delete(COMPANY_ID, USER_ID_1, password, JOB_IDENTIFIER);

        // then
        assertThatThrownBy(() -> jobSurveyManager.findJobByIdentifier(COMPANY_ID, JOB_IDENTIFIER))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found");

    }

    @Test
    void shouldThrowException_WhenDeletingJobNonExistent() {
        // given
        String password = "Password123";

        // when, then
        assertThatThrownBy(() -> jobSurveyManager.delete(COMPANY_ID, USER_ID_1, password, "NON-EXISTENT-JOB"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found");
    }


    // helper

    private InputStream getStream(String filename) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        assertThat(stream).as("Test file not found: [%s]".formatted(filename)).isNotNull();
        return stream;
    }

    private LevelingReport reportOneWayDoubleLeveling(MultipartFile uploadFileSecond, OffsetDateTime observationTime) {
        return jobSurveyManager.processLevelingFile(
                COMPANY_ID,
                JOB_IDENTIFIER,
                START_H,
                END_H,
                uploadFileSecond,
                LevelingType.ONE_WAY_DOUBLE,
                observationTime
        );
    }

    private LevelingReport reportOneWayLeveling(MultipartFile uploadFileFirst, OffsetDateTime observationTime) {
        return jobSurveyManager.processLevelingFile(
                COMPANY_ID,
                JOB_IDENTIFIER,
                START_H,
                END_H,
                uploadFileFirst,
                LevelingType.ONE_WAY,
                observationTime

        );
    }
}
