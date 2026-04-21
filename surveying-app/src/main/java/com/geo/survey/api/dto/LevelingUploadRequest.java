package com.geo.survey.api.dto;

import com.geo.survey.math.value.LevelingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

public record LevelingUploadRequest(
        @NotBlank(message = "Job identifier must not be blank")
        @Size(max = 100, message = "Job identifier must not exceed 100 characters")
        String jobIdentifier,

        @NotNull(message = "File must not be null")
        MultipartFile file,

        @NotNull(message = "Leveling type must not be null")
        LevelingType type,

        Double startH,

        Double endH,

        @NotNull(message = "Observation time must not be null")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        OffsetDateTime observationTime
) {
}
