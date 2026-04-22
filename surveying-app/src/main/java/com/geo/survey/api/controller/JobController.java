package com.geo.survey.api.controller;

import com.geo.survey.api.dto.*;
import com.geo.survey.api.mapper.JobApiMapper;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.JobListItem;
import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.domain.service.JobSurveyManager;
import com.geo.survey.infrastructure.security.CustomUserDetails;
import com.geo.survey.infrastructure.security.annotation.IsAdmin;
import com.geo.survey.infrastructure.security.annotation.IsAdminOrSurveyor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Jobs", description = "Job and leveling survey management")
@Validated
@IsAdminOrSurveyor
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobSurveyManager jobSurveyManager;
    private final JobApiMapper mapper;

    @Operation(
            summary = "Create job",
            description = "Creates a new survey job for the authenticated user's company"
    )
    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateJobRequest request
    ) {
        Job job = mapper.toJob(request);
        Job created = jobSurveyManager.createJob(job, userDetails.getCompanyId(), userDetails.getUserId());
        URI location = URI.create("/api/v1/jobs?jobIdentifier=%s".formatted(created.getJobIdentifier()));
        return ResponseEntity.created(location).body(mapper.toJobResponse(created));
    }

    @Operation(
            summary = "Get job by identifier",
            description = "Returns job details for a given job identifier within the company"
    )
    @GetMapping
    public ResponseEntity<JobResponse> getJobByIdentifier(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @NotBlank String jobIdentifier
    ) {
        Long companyId = userDetails.getCompanyId();
        Job job = jobSurveyManager.findJobByIdentifier(companyId, jobIdentifier);
        return ResponseEntity.ok(mapper.toJobResponse(job));
    }

    @Operation(
            summary = "Delete job",
            description = "Deletes a job and all associated reports. Requires password confirmation"
    )
    @IsAdmin
    @DeleteMapping
    public ResponseEntity<Void> deleteJob(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeleteJobRequest request
    ) {
        jobSurveyManager.delete(
                userDetails.getCompanyId(),
                userDetails.getUserId(),
                request.password(),
                request.jobIdentifier()
        );
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Process leveling file",
            description = "Uploads a CSV or TXT file with leveling observations and calculates the report"
    )
    @PostMapping(value = "/leveling", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LevelingReportResponse> processLevelingFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute LevelingUploadRequest request
    ) {
        LevelingReport levelingReport = jobSurveyManager.processLevelingFile(
                userDetails.getCompanyId(),
                request.jobIdentifier(),
                request.startH(),
                request.endH(),
                request.file(),
                request.type(),
                request.observationTime());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toLevelingReportResponse(levelingReport));
    }

    @Operation(
            summary = "Get leveling reports",
            description = "Returns paginated leveling reports for a given job identifier"
    )
    @GetMapping("/leveling")
    public ResponseEntity<Page<LevelingReportResponse>> getLevelingReports(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @NotBlank String jobIdentifier,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        int safeSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("id").ascending());
        Page<LevelingReport> reports = jobSurveyManager
                .findLevelingReports(jobIdentifier, userDetails.getCompanyId(), pageable);
        return ResponseEntity.ok(reports.map(mapper::toLevelingReportResponse));
    }

    @Operation(
            summary = "Get company jobs",
            description = "Returns paginated list of all jobs for the authenticated user's company"
    )
    @GetMapping("/company")
    public ResponseEntity<Page<JobListItemDto>> getJobsForCompany(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        int safeSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("id").descending());
        Page<JobListItem> jobs = jobSurveyManager.getAllJobsForCompany(userDetails.getCompanyId(), pageable);
        return ResponseEntity.ok(jobs.map(mapper::toListItemDto));
    }

    @Operation(
            summary = "Get user jobs",
            description = "Returns paginated list of jobs created by the authenticated user"
    )
    @GetMapping("/user")
    public ResponseEntity<Page<JobListItemDto>> getJobsForUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        int safeSize = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("id").descending());
        Page<JobListItem> jobs = jobSurveyManager.getAllJobsForUser(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(jobs.map(mapper::toListItemDto));
    }

}
