package com.geo.survey.api.controller;

import com.geo.survey.api.dto.CreateJobRequest;
import com.geo.survey.api.dto.JobResponse;
import com.geo.survey.api.dto.LevelingReportResponse;
import com.geo.survey.api.dto.LevelingUploadRequest;
import com.geo.survey.api.mapper.JobApiMapper;
import com.geo.survey.domain.model.Job;
import com.geo.survey.domain.model.LevelingReport;
import com.geo.survey.domain.service.JobSurveyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobSurveyManager jobSurveyManager;
    private final JobApiMapper mapper;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody CreateJobRequest request) {
        Job job = mapper.toJob(request);
        Job created = jobSurveyManager.createJob(job, request.companyId(), request.userId());
        URI location = URI.create("/api/v1/jobs?jobIdentifier=" + created.getJobIdentifier());
        return ResponseEntity.created(location).body(mapper.toJobResponse(created));
    }

    @GetMapping
    public ResponseEntity<JobResponse> getReportsByJobIdentifier(@RequestParam String jobIdentifier) {
        Job job = jobSurveyManager.findJobByIdentifier(jobIdentifier);
        return ResponseEntity.ok(mapper.toJobResponse(job));
    }

    @PostMapping(value = "/leveling", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LevelingReportResponse> processLevelingFile(
            @ModelAttribute LevelingUploadRequest request
    ) {
        LevelingReport levelingReport = jobSurveyManager.processLevelingFile(
                request.jobIdentifier(),
                request.startH(),
                request.endH(),
                request.file(),
                request.type(),
                request.observationTime());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toLevelingReportResponse(levelingReport));
    }

    @GetMapping("/leveling")
    public ResponseEntity<List<LevelingReportResponse>> getLevelingReports(@RequestParam String jobIdentifier) {
        List<LevelingReport> reports = jobSurveyManager.findLevelingReports(jobIdentifier);
        return ResponseEntity.ok(mapper.toLevelingReportResponseList(reports));
    }
}
