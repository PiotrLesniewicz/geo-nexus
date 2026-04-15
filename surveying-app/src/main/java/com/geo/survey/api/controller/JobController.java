package com.geo.survey.api.controller;

import com.geo.survey.api.dto.CreateJobRequest;
import com.geo.survey.api.dto.JobResponse;
import com.geo.survey.api.dto.LevelingReportResponse;
import com.geo.survey.api.dto.LevelingUploadRequest;
import com.geo.survey.api.mapper.JobApiMapper;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.domain.service.JobSurveyManager;
import com.geo.survey.infrastructure.security.CustomUserDetails;
import com.geo.survey.infrastructure.security.annotation.IsAdminOrSurveyor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@IsAdminOrSurveyor
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobSurveyManager jobSurveyManager;
    private final JobApiMapper mapper;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateJobRequest request
    ) {
        Job job = mapper.toJob(request);
        Job created = jobSurveyManager.createJob(job, userDetails.getCompanyId(), userDetails.getUserId());
        URI location = URI.create("/api/v1/jobs?jobIdentifier=%s".formatted(created.getJobIdentifier()));
        return ResponseEntity.created(location).body(mapper.toJobResponse(created));
    }

    @GetMapping
    public ResponseEntity<JobResponse> getJobByIdentifier(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String jobIdentifier
    ) {
        Long companyId = userDetails.getCompanyId();
        Job job = jobSurveyManager.findJobByIdentifier(companyId, jobIdentifier);
        return ResponseEntity.ok(mapper.toJobResponse(job));
    }

    @PostMapping(value = "/leveling", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LevelingReportResponse> processLevelingFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute LevelingUploadRequest request
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

    @GetMapping("/leveling")
    public ResponseEntity<Page<LevelingReportResponse>> getLevelingReports(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String jobIdentifier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LevelingReport> reports = jobSurveyManager
                .findLevelingReports(jobIdentifier, userDetails.getCompanyId(), pageable);
        return ResponseEntity.ok(reports.map(mapper::toLevelingReportResponse));
    }
}
