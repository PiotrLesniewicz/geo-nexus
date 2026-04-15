package com.geo.survey.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geo.survey.api.controller.JobController;
import com.geo.survey.api.dto.CreateJobRequest;
import com.geo.survey.api.mapper.JobApiMapperImpl;
import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.*;
import com.geo.survey.domain.service.JobSurveyManager;
import com.geo.survey.infrastructure.security.CustomUserDetails;
import com.geo.survey.infrastructure.security.CustomUserDetailsService;
import com.geo.survey.infrastructure.security.JwtService;
import com.geo.survey.math.value.LevelingType;
import com.geo.survey.testconfig.SecurityTestConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {JobController.class})
@Import({
        JobApiMapperImpl.class,
        SecurityTestConfiguration.class
})
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobSurveyManager jobSurveyManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    private static final String JOB_IDENTIFIER = "JOB-2026/1/1";

    // POST /api/v1/jobs — createJob

    @Test
    void shouldReturn201_whenCreateJob() throws Exception {
        // given
        CreateJobRequest request = buildCreateJobRequest();
        when(jobSurveyManager.createJob(any(Job.class), anyLong(), anyLong()))
                .thenReturn(buildJob());

        // when, then
        mockMvc.perform(post("/api/v1/jobs")
                        .with(user(getUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(jobSurveyManager).createJob(any(Job.class), anyLong(), anyLong());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "duplicate identifier, Job already exists with identifier: [JOB-001]",
            "inactive company,     Company is not active"
    })
    void shouldReturn409_whenCreateJobThrowsBusinessRuleViolation(
            String scenario,
            String exceptionMessage) throws Exception {
        // given
        CreateJobRequest request = buildCreateJobRequest();
        doThrow(new BusinessRuleViolationException(exceptionMessage))
                .when(jobSurveyManager).createJob(any(), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/jobs")
                        .with(user(getUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404_whenCreateJobForNonExistentUser() throws Exception {
        // given
        CreateJobRequest request = buildCreateJobRequest();
        doThrow(new ResourceNotFoundException("User not found"))
                .when(jobSurveyManager).createJob(any(), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/jobs")
                        .with(user(getUserDetails()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // POST /api/v1/jobs/leveling — processLevelingFile

    @Test
    void shouldReturn201_whenProcessLevelingFile() throws Exception {
        // given
        MockMultipartFile file = buildMockFile();
        when(jobSurveyManager.processLevelingFile(
                any(), any(), any(), any(), any(MultipartFile.class), eq(LevelingType.ONE_WAY), any()))
                .thenReturn(buildLevelingReport());

        // when, then

        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(file)
                        .with(user(getUserDetails()))
                        .param("jobIdentifier", JOB_IDENTIFIER)
                        .param("type", "ONE_WAY")
                        .param("startH", "100.0")
                        .param("endH", "105.0")
                        .param("observationTime", "2024-06-15T08:30:00+02:00"))
                .andExpect(status().isCreated());

        verify(jobSurveyManager).processLevelingFile(
                any(), any(), any(), any(), any(MultipartFile.class), eq(LevelingType.ONE_WAY), any());
    }

    @Test
    void shouldReturn201_whenProcessLevelingFileWithoutStartEndH() throws Exception {
        // given — startH/endH is null
        MockMultipartFile file = buildMockFile();
        when(jobSurveyManager.processLevelingFile(
                anyLong(), anyString(), isNull(), isNull(), any(MultipartFile.class), eq(LevelingType.ONE_WAY), any()))
                .thenReturn(buildLevelingReport());

        // when, then
        mockMvc.perform(multipart("/api/v1/jobs/leveling")
                        .file(file)
                        .with(user(getUserDetails()))
                        .param("jobIdentifier", JOB_IDENTIFIER)
                        .param("type", "ONE_WAY")
                        .param("observationTime", "2024-06-15T08:30:00+02:00"))
                .andExpect(status().isCreated());
        verify(jobSurveyManager).processLevelingFile(
                any(), any(), isNull(), isNull(), any(MultipartFile.class), eq(LevelingType.ONE_WAY), any());
    }

    // GET /api/v1/jobs — getReports

    @Test
    void shouldReturn200WithReports_whenGetJobByIdentifier() throws Exception {
        // given
        when(jobSurveyManager.findJobByIdentifier(anyLong(), anyString()))
                .thenReturn(Job.builder().build());

        // when, then
        mockMvc.perform(get("/api/v1/jobs")
                        .with(user(getUserDetails()))
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isOk());

    }

    @Test
    void shouldReturn404_whenJobNonExisting() throws Exception {
        // given
        when(jobSurveyManager.findJobByIdentifier(anyLong(), anyString()))
                .thenThrow(new ResourceNotFoundException("Job not found with identifier: [%s]".formatted(JOB_IDENTIFIER)));

        // when, then
        mockMvc.perform(get("/api/v1/jobs")
                        .with(user(getUserDetails()))
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldReturn404_whenGetLevelingReportsForNonExistentJob() throws Exception {
        // given
        when(jobSurveyManager.findLevelingReports(eq(JOB_IDENTIFIER), anyLong(), any()))
                .thenThrow(new ResourceNotFoundException("Not found reports with job identifier: [%s]".formatted(JOB_IDENTIFIER)));

        // when, then
        mockMvc.perform(get("/api/v1/jobs/leveling")
                        .with(user(getUserDetails()))
                        .param("jobIdentifier", JOB_IDENTIFIER))
                .andExpect(status().isNotFound());

    }

    // Helpers

    private static @NotNull CustomUserDetails getUserDetails() {
        return new CustomUserDetails(
                1L,
                1L,
                "admin",
                "password",
                Role.SURVEYOR,
                true,
                true,
                false
        );
    }

    private static CreateJobRequest buildCreateJobRequest() {
        return CreateJobRequest.builder()
                .jobIdentifier(JOB_IDENTIFIER)
                .description("Niwelacja terenu")
                .street("Miernicza")
                .buildingNumber("5")
                .postalCode("00-001")
                .city("Warszawa")
                .country("Polska")
                .build();
    }

    private static Job buildJob() {
        return Job.builder()
                .id(1L)
                .jobIdentifier(JOB_IDENTIFIER)
                .description("Niwelacja terenu")
                .status(StatusJob.OPEN)
                .address(Address.builder()
                        .street("Miernicza")
                        .buildingNumber("5")
                        .postalCode("00-001")
                        .city("Warszawa")
                        .country("Polska")
                        .build())
                .createdAt(OffsetDateTime.parse("2024-06-15T08:00:00+02:00"))
                .company(Company.builder().id(1L).build())
                .user(User.builder().id(10L).build())
                .build();
    }

    private static LevelingReport buildLevelingReport() {
        return LevelingReport.builder()
                .id(1L)
                .levelingType(LevelingType.ONE_WAY)
                .startHeight(BigDecimal.valueOf(100.0))
                .endHeight(BigDecimal.valueOf(105.0))
                .measuredDifference(BigDecimal.valueOf(5.0))
                .theoreticalDifference(BigDecimal.valueOf(5.0))
                .misclosure(BigDecimal.ZERO)
                .allowedMisclosure(BigDecimal.valueOf(0.012))
                .toleranceMet(true)
                .sequenceDistance(BigDecimal.valueOf(250.0))
                .observationTime(OffsetDateTime.parse("2024-06-15T08:30:00+02:00"))
                .generatedAt(OffsetDateTime.parse("2024-06-15T09:00:00+02:00"))
                .stations(List.of())
                .job(Job.builder().id(1L).build())
                .build();
    }

    private static MockMultipartFile buildMockFile() {
        return new MockMultipartFile(
                "file",
                "data.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "1,100.0,101.5,50,60\n2,101.5,103.0,55,65".getBytes()
        );
    }
}
