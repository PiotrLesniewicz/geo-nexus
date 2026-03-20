package com.geo.survey.api.dto;

public record ErrorResponse(String errorId) {
    public static ErrorResponse of(String errorId) {
        return new ErrorResponse(errorId);
    }
}
