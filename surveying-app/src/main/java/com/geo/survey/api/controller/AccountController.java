package com.geo.survey.api.controller;

import com.geo.survey.api.dto.*;
import com.geo.survey.api.mapper.AccountApiMapper;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.model.UserSummary;
import com.geo.survey.domain.service.AccountManager;
import com.geo.survey.infrastructure.security.CustomUserDetails;
import com.geo.survey.infrastructure.security.annotation.IsAdmin;
import com.geo.survey.infrastructure.security.annotation.IsAdminOrSurveyor;
import com.geo.survey.infrastructure.security.annotation.IsSuperAdmin;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class AccountController {

    private final AccountManager accountManager;
    private final AccountApiMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<Void> registerCompanyWithAdmin(
            @Valid @RequestBody RegisterCompanyRequest request
    ) {
        Company company = mapper.toCompany(request);
        User admin = mapper.toAdminUser(request);
        accountManager.registerCompanyWithAdmin(company, admin, request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @IsAdmin
    @PostMapping("/users")
    public ResponseEntity<Void> registerUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegisterUserRequest request) {
        User user = mapper.toUser(request);
        accountManager.registerUser(userDetails.getCompanyId(), user, request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @IsAdmin
    @DeleteMapping("/users/{email}")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @Email @NotBlank String email) {
        accountManager.deleteUser(userDetails.getCompanyId(), email);
        return ResponseEntity.noContent().build();
    }

    @IsAdmin
    @PatchMapping("/users/{email}/role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @Email @NotBlank String email,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        accountManager.changeRole(email, userDetails.getCompanyId(), request.role());
        return ResponseEntity.noContent().build();
    }

    @IsAdmin
    @GetMapping("/users/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @Email @NotBlank String email
    ) {
        UserSummary user = accountManager.getUserSummary(userDetails.getCompanyId(), email);
        return ResponseEntity.ok(mapper.toUserDto(user));
    }

    @IsAdminOrSurveyor
    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDto> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserSummary user = accountManager.getUserSummary(userDetails.getUserId());
        return ResponseEntity.ok(mapper.toUserDto(user));
    }

    @IsAdminOrSurveyor
    @PatchMapping("/users/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        accountManager.changePassword(userDetails.getUserId(), request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @IsSuperAdmin
    @PatchMapping("/{nip}/block")
    public ResponseEntity<Void> blockCompany(
            @PathVariable @Pattern(regexp = "\\d{10}") String nip
    ) {
        accountManager.blockCompany(nip);
        return ResponseEntity.noContent().build();
    }

    @IsSuperAdmin
    @PatchMapping("/{nip}/activate")
    public ResponseEntity<Void> activateCompany(
            @PathVariable @Pattern(regexp = "\\d{10}") String nip
    ) {
        accountManager.activateCompany(nip);
        return ResponseEntity.noContent().build();
    }
}
