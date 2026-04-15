package com.geo.survey.integration;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/db/migration/cleanup.sql", "/db/migration/test_api_job.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JobControllerIntegrationTest extends TestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Clock clock;

    private static final String JOB_IDENTIFIER = "JOB-2024-001"; // from test_api_job.sql
    private static final String TEST_USER_EMAIL = "anna.nowak@geosurvey.pl"; // 'surveyor' from test_api_job.sql
    @BeforeEach
    void setUp() {
        Mockito.when(clock.instant()).thenReturn(Instant.parse("2024-06-15T10:00:00Z"));
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    @WithUserDetails(TEST_USER_EMAIL)
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

    @Test
    @WithUserDetails(TEST_USER_EMAIL)
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
    @WithUserDetails(TEST_USER_EMAIL)
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
    @WithUserDetails(TEST_USER_EMAIL)
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
    @WithUserDetails(TEST_USER_EMAIL)
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
    @WithUserDetails(TEST_USER_EMAIL)
    void shouldReturn409_whenProcessingFileForClosedJob() throws Exception {
        // given
        String closedJobId = "JOB-2024-CLOSED"; // closed job from test_api_job.sql
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
    @WithUserDetails(TEST_USER_EMAIL)
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