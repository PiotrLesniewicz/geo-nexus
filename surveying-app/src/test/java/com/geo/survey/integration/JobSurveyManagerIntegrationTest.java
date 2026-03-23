package com.geo.survey.integration;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.domain.model.LevelingStation;
import com.geo.survey.domain.model.StatusJob;
import com.geo.survey.domain.service.JobSurveyManager;
import com.geo.survey.math.value.LevelingType;
import com.geo.survey.testconfig.TestContainerConfig;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AllArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/db/cleanup.sql", "/db/test_data_leveling.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JobSurveyManagerIntegrationTest extends TestContainerConfig {

    private JobSurveyManager jobSurveyManager;

    @MockitoBean
    private Clock clock;

    private static final Long JOB_ID = 1L;           // from test_data_leveling.sql
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
        Long companyId = 1L; // from test_data_leveling.sql
        Long userId = 1L;    // from test_data_leveling.sql

        Job job = Job.builder()
                .jobIdentifier("JOB-2024-NEW")
                .description("New test measurement")
                .build();

        // when
        Job result = jobSurveyManager.createJob(job, companyId, userId);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getJobIdentifier()).isEqualTo("JOB-2024-NEW");
        assertThat(result.getCompany().getId()).isEqualTo(companyId);
        assertThat(result.getUser().getId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(StatusJob.OPEN);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    // ONE_WAY leveling tests

    @Test
    void shouldProcessOneWayLevelingFile_andSaveReportToDatabase() {
        // given
        InputStream stream = getStream("leveling/one_way_10stations.csv");
        OffsetDateTime observationTime = OffsetDateTime.now(clock);
        BigDecimal expectedMisclosure = new BigDecimal("0.0020"); // calculated manually
        BigDecimal expectedSequenceDistance = new BigDecimal("0.4860");

        // when
        LevelingReport result = jobSurveyManager.processLevelingFile(
                JOB_ID, START_H, END_H, stream, "one_way_10stations.csv",
                LevelingType.ONE_WAY, observationTime);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getLevelingType()).isEqualTo(LevelingType.ONE_WAY);
        assertThat(result.getMisclosure()).isEqualByComparingTo(expectedMisclosure);
        assertThat(result.getSequenceDistance()).isEqualByComparingTo(expectedSequenceDistance);

        assertThat(result.getJob().getId()).isEqualTo(JOB_ID);
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
    void shouldProcessOneWayLevelingFile_withoutStartAndEndH() {
        InputStream stream = getStream("leveling/one_way_10stations.csv");
        OffsetDateTime observationTime = OffsetDateTime.now(clock);

        // when
        LevelingReport result = jobSurveyManager.processLevelingFile(
                JOB_ID, null, null, stream, "one_way_10stations.csv",
                LevelingType.ONE_WAY, observationTime);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStartHeight()).isNull();
        assertThat(result.getEndHeight()).isNull();
        assertThat(result.getStations()).hasSize(10);
    }

    // ONE_WAY_DOUBLE leveling tests

    @Test
    void shouldProcessOneWayDoubleLevelingFile_andSaveReportToDatabase() {
        // given
        InputStream stream = getStream("leveling/one_way_double_10stations.csv");
        OffsetDateTime observationTime = OffsetDateTime.now(clock);
        BigDecimal expectedMisclosure = new BigDecimal("-0.0025"); // calculated manually
        BigDecimal expectedSequenceDistance = new BigDecimal("0.4860");

        // when
        LevelingReport result = jobSurveyManager.processLevelingFile(
                JOB_ID, START_H, END_H, stream, "one_way_double_10stations.csv",
                LevelingType.ONE_WAY_DOUBLE, observationTime);

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
    void shouldProcessTwoLevelingReports_forSameJob() {
        // given
        InputStream streamFirst = getStream("leveling/one_way_10stations.csv");
        InputStream streamSecond = getStream("leveling/one_way_double_10stations.csv");
        OffsetDateTime observationTime = OffsetDateTime.now(clock);

        BigDecimal expectedMisclosureFirst = new BigDecimal("0.0020");
        BigDecimal expectedMisclosureSecond = new BigDecimal("-0.0025");

        // when
        LevelingReport firstReport = jobSurveyManager.processLevelingFile(
                JOB_ID, START_H, END_H, streamFirst, "one_way_10stations.csv",
                LevelingType.ONE_WAY, observationTime);

        LevelingReport secondReport = jobSurveyManager.processLevelingFile(
                JOB_ID, START_H, END_H, streamSecond, "one_way_double_10stations.csv",
                LevelingType.ONE_WAY_DOUBLE, observationTime);

        // then
        assertThat(firstReport.getId()).isNotNull();
        assertThat(secondReport.getId()).isNotNull();
        assertThat(firstReport.getId()).isNotEqualTo(secondReport.getId());


        assertThat(firstReport.getJob().getId()).isEqualTo(JOB_ID);
        assertThat(secondReport.getJob().getId()).isEqualTo(JOB_ID);


        assertThat(firstReport.getLevelingType()).isEqualTo(LevelingType.ONE_WAY);
        assertThat(firstReport.getMisclosure()).isEqualByComparingTo(expectedMisclosureFirst);
        assertThat(firstReport.getStations()).hasSize(10);

        assertThat(secondReport.getLevelingType()).isEqualTo(LevelingType.ONE_WAY_DOUBLE);
        assertThat(secondReport.getMisclosure()).isEqualByComparingTo(expectedMisclosureSecond);
        assertThat(secondReport.getStations()).hasSize(10);


        assertThat(firstReport.getStations().getFirst().getBacksightElev2()).isNull();
        assertThat(secondReport.getStations().getFirst().getBacksightElev2()).isNotNull();

        List<LevelingReport> reportsForJob = jobSurveyManager.findReportForJobId(JOB_ID);
        assertThat(reportsForJob).hasSize(2);
    }

    // createJob business rule validation tests

    @Test
    void shouldThrowException_WhenCreatingJobWithInactiveCompany() {
        // given
        Long inactiveCompanyId = 3L; // TerraMap Biuro Geodezji - active = FALSE from test_data_leveling.sql
        Long userId = 1L;

        Job job = Job.builder()
                .jobIdentifier("JOB-2024-INACTIVE-COMPANY")
                .description("Job with inactive company")
                .build();

        // when & then
        assertThatThrownBy(() -> jobSurveyManager.createJob(job, inactiveCompanyId, userId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Company is not active");
    }

    @Test
    void shouldThrowException_WhenCreatingJobWithInactiveUser() {
        // given
        Long companyId = 1L;
        Long inactiveUserId = 3L; // piotr.wisniewski@geodeta.pl - active = FALSE from test_data_leveling.sql

        Job job = Job.builder()
                .jobIdentifier("JOB-2024-INACTIVE-USER")
                .description("Job with inactive user")
                .build();

        // when & then
        assertThatThrownBy(() -> jobSurveyManager.createJob(job, companyId, inactiveUserId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("User is not active");
    }

    // processLevelingFile business rule validation tests

    @Test
    void shouldThrowException_WhenProcessingLevelingFileForClosedJob() {
        // given
        Long closedJobId = 3L; // closed job from test_data_leveling.sql
        InputStream stream = getStream("leveling/one_way_10stations.csv");
        OffsetDateTime observationTime = OffsetDateTime.now(clock);

        // when & then
        assertThatThrownBy(() -> jobSurveyManager.processLevelingFile(
                closedJobId, START_H, END_H, stream, "one_way_10stations.csv",
                LevelingType.ONE_WAY, observationTime))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Job is not open");
    }

    // helper

    private InputStream getStream(String filename) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        assertThat(stream).as("Test file not found: " + filename).isNotNull();
        return stream;
    }
}
