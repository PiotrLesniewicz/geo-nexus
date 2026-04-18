package com.geo.survey.api.dto;

public record ChangePasswordRequest(String oldPassword, String newPassword) {
}
