package com.geo.survey.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geo.survey.api.dto.DeleteJobRequest;
import com.geo.survey.math.value.LevelingType;
import com.geo.survey.testconfig.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/db/migration/cleanup.sql", "/db/migration/test_data_job_leveling.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JobControllerIntegrationTest extends TestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Clock clock;

    private static final String JOB_IDENTIFIER = "JOB-2024-001"; // from test_data_job_leveling.sql
    private static final String SURVEYOR_EMAIL = "anna.nowak@geosurvey.pl"; // 'surveyor' from test_data_job_leveling.sql
    private static final String ADMIN_EMAIL = "jan.kowalski@geosurvey.pl"; // 'admin' from test_data_job_leveling.sql

    @BeforeEach
    void setUp() {
        Mockito.when(clock.instant()).thenReturn(Instant.parse("2024-06-15T10:00:00Z"));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    // get job

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturnJob_withCorrectAddress() throws Exception {

        mockMvc.perform(get("/api/v1/jobs")
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isNotEmpty())
                .andExpect(jsonPath("$.street").isNotEmpty())
                .andExpect(jsonPath("$.country").isNotEmpty())
                .andExpect(jsonPath("$.nip").isNotEmpty())
                .andExpect(jsonPath("$.userName").isNotEmpty());
    }

    // process leveling

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldProcessOneWayLevelingFile_andReturnReport() throws Exception {
        // given
        MockMultipartFile file = buildMultipartFile("leveling/one_way_10stations.csv", "data.csv");

        // when, then
        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(file)
                        .param("jobIdentifier", JOB_IDENTIFIER)
                        .param("type", LevelingType.ONE_WAY.name())
                        .param("startH", "100.0")
                        .param("endH", "100.5")
                        .param("observationTime", "2024-06-15T08:30:00+00:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobIdentifier").value(JOB_IDENTIFIER))
                .andExpect(jsonPath("$.levelingType").value("ONE_WAY"))
                .andExpect(jsonPath("$.misclosure").isNotEmpty())
                .andExpect(jsonPath("$.sequenceDistance").isNotEmpty())
                .andExpect(jsonPath("$.stations.length()").value(10))
                .andExpect(jsonPath("$.stations[0].adjustedHeight").isNotEmpty())
                .andExpect(jsonPath("$.stations[0].correction").isNotEmpty());
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldProcessOneWayDoubleLevelingFile_andReturnReport() throws Exception {
        // given
        MockMultipartFile file = buildMultipartFile("leveling/one_way_double_10stations.csv", "data.csv");

        // when, then
        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(file)
                        .param("jobIdentifier", JOB_IDENTIFIER)
                        .param("type", LevelingType.ONE_WAY_DOUBLE.name())
                        .param("startH", "100.0")
                        .param("endH", "100.5")
                        .param("observationTime", "2024-06-15T08:30:00+00:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.levelingType").value(LevelingType.ONE_WAY_DOUBLE.name()))
                .andExpect(jsonPath("$.misclosure").isNotEmpty())
                .andExpect(jsonPath("$.stations.length()").value(10))
                .andExpect(jsonPath("$.stations[0].stationError").isNotEmpty());
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldProcessLevelingFile_withoutStartAndEndH() throws Exception {
        // given
        MockMultipartFile file = buildMultipartFile("leveling/one_way_10stations.csv", "data.csv");

        // when, then
        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(file)
                        .param("jobIdentifier", JOB_IDENTIFIER)
                        .param("type", LevelingType.ONE_WAY.name())
                        .param("observationTime", "2024-06-15T08:30:00+00:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startHeight").doesNotExist())
                .andExpect(jsonPath("$.endHeight").doesNotExist())
                .andExpect(jsonPath("$.stations.length()").value(10));
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturnReports_afterProcessingTwoFiles() throws Exception {
        // given
        MockMultipartFile fileOne = buildMultipartFile("leveling/one_way_10stations.csv", "one.csv");
        MockMultipartFile fileTwo = buildMultipartFile("leveling/one_way_double_10stations.csv", "two.csv");

        // when
        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(fileOne)
                        .param("jobIdentifier", JOB_IDENTIFIER)
                        .param("type", LevelingType.ONE_WAY.name())
                        .param("startH", "100.0")
                        .param("endH", "100.5")
                        .param("observationTime", "2024-06-15T08:30:00+00:00"))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(fileTwo)
                        .param("jobIdentifier", JOB_IDENTIFIER)
                        .param("type", LevelingType.ONE_WAY_DOUBLE.name())
                        .param("startH", "100.0")
                        .param("endH", "100.5")
                        .param("observationTime", "2024-06-15T08:30:00+00:00"))
                .andExpect(status().isCreated());

        // then - GET expected two leveling reports
        mockMvc.perform(get("/api/v1/jobs/leveling")
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].levelingType").value("ONE_WAY"))
                .andExpect(jsonPath("$.content[1].levelingType").value("ONE_WAY_DOUBLE"));
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturn409_whenProcessingFileForClosedJob() throws Exception {
        // given
        String closedJobId = "JOB-2024-CLOSED"; // closed job from test_data_job_leveling.sql
        MockMultipartFile file = buildMultipartFile("leveling/one_way_10stations.csv", "data.csv");

        // when, then
        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(file)
                        .param("jobIdentifier", closedJobId)
                        .param("type", LevelingType.ONE_WAY.name())
                        .param("observationTime", "2024-06-15T08:30:00+00:00"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturn404_whenProcessingFileForNonExistentJob() throws Exception {
        // given
        MockMultipartFile file = buildMultipartFile("leveling/one_way_10stations.csv", "data.csv");

        // when, then
        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(file)
                        .param("jobIdentifier", "NON_EXISTENT_JOB")
                        .param("type", LevelingType.ONE_WAY.name())
                        .param("observationTime", "2024-06-15T08:30:00+00:00"))
                .andExpect(status().isNotFound());
    }

    // delete

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldCorrectlyDeletedJob() throws Exception {
        String password = "Password123";
        DeleteJobRequest request = new DeleteJobRequest(password, JOB_IDENTIFIER);
        mockMvc.perform(delete("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldThrowException_WhenPasswordIncorrect() throws Exception {
        String password = "wrong_password";
        DeleteJobRequest request = new DeleteJobRequest(password, JOB_IDENTIFIER);
        mockMvc.perform(delete("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // get all jobs item for company/user

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturnCompanyJobs() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(7))
                .andExpect(jsonPath("$.content[0].jobIdentifier").isNotEmpty())
                .andExpect(jsonPath("$.content[0].city").isNotEmpty())
                .andExpect(jsonPath("$.content[0].status").isNotEmpty());
    }

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldReturnUserJobs() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(7))
                .andExpect(jsonPath("$.content[0].jobIdentifier").isNotEmpty())
                .andExpect(jsonPath("$.content[0].city").isNotEmpty());
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldSupportPagination_withValidPageAndSize() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/company")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.totalPages").value(4));
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldLimitSize_toMaximum50() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/company")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(7));
    }

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldReturnEmptyPage_whenNoJobsExist() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/user")
                        .param("page", "10")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(7));
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturnCompanyJobsSortedByIdDesc() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobIdentifier").value("TEST-JOB-005"))
                .andExpect(jsonPath("$.content[1].jobIdentifier").value("TEST-JOB-004"));
    }

    // PATCH /api/v1/jobs/close

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldCloseJob_andReturn204() throws Exception {
        mockMvc.perform(patch("/api/v1/jobs/close")
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldReturn409_whenClosingAlreadyClosedJob() throws Exception {
        String closedJobId = "JOB-2024-CLOSED";
        mockMvc.perform(patch("/api/v1/jobs/close", closedJobId)
                        .param("jobIdentifier", closedJobId))
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturn404_whenClosingNonExistentJob() throws Exception {
        String nonExistentJob = "NON_EXISTENT_JOB";
        mockMvc.perform(patch("/api/v1/jobs/close", nonExistentJob)
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isNotFound());
    }

// PATCH /api/v1/jobs/open

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldOpenJob_andReturn204() throws Exception {
        String closedJobId = "JOB-2024-CLOSED";
        mockMvc.perform(patch("/api/v1/jobs/open")
                        .param("jobIdentifier", closedJobId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails(ADMIN_EMAIL)
    void shouldReturn409_whenOpeningAlreadyOpenJob() throws Exception {
        mockMvc.perform(patch("/api/v1/jobs/open", JOB_IDENTIFIER)
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(SURVEYOR_EMAIL)
    void shouldReturn404_whenOpeningNonExistentJob() throws Exception {
        String nonExistentJob = "NON_EXISTENT_JOB";
        mockMvc.perform(patch("/api/v1/jobs/open", nonExistentJob)
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isNotFound());
    }

    // helper

    private MockMultipartFile buildMultipartFile(String resourcePath, String filename) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(stream).as("Test file not found: [%s]".formatted(resourcePath)).isNotNull();
        try (stream) {
            return new MockMultipartFile("file", filename, MediaType.TEXT_PLAIN_VALUE, stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to build multipart file from: [%s]".formatted(resourcePath), e);
        }
    }
}