package com.geo.survey.api.dto;

import com.geo.survey.math.value.LevelingType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

public record LevelingUploadRequest(
        String jobIdentifier,
        MultipartFile file,
        LevelingType type,
        Double startH,
        Double endH,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime observationTime
) {
}
