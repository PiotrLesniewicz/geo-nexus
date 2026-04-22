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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Account", description = "Company and user account management")
@Validated
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class AccountController {

    private final AccountManager accountManager;
    private final AccountApiMapper mapper;

    @Operation(
            summary = "Register company with admin",
            description = "Creates a new company account along with the first admin user"
    )
    @PostMapping("/register")
    public ResponseEntity<Void> registerCompanyWithAdmin(
            @Valid @RequestBody RegisterCompanyRequest request
    ) {
        Company company = mapper.toCompany(request);
        User admin = mapper.toAdminUser(request);
        accountManager.registerCompanyWithAdmin(company, admin, request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Register user",
            description = "Registers a new user within the authenticated admin's company"
    )
    @IsAdmin
    @PostMapping("/users")
    public ResponseEntity<Void> registerUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegisterUserRequest request) {
        User user = mapper.toUser(request);
        accountManager.registerUser(userDetails.getCompanyId(), user, request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Delete user",
            description = "Soft-deletes a user by email within the authenticated admin's company"
    )
    @IsAdmin
    @DeleteMapping("/users/{email}")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @Email @NotBlank String email) {
        accountManager.deleteUser(userDetails.getCompanyId(), email);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Change user role",
            description = "Changes the role of a user within the company. Cannot demote the last active admin"
    )
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

    @Operation(
            summary = "Get user by email",
            description = "Returns user details and job statistics for a given email within the company"
    )
    @IsAdmin
    @GetMapping("/users/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable @Email @NotBlank String email
    ) {
        UserSummary user = accountManager.getUserSummary(userDetails.getCompanyId(), email);
        return ResponseEntity.ok(mapper.toUserDto(user));
    }

    @Operation(
            summary = "Get own profile",
            description = "Returns the profile and job statistics of the currently authenticated user"
    )
    @IsAdminOrSurveyor
    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDto> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserSummary user = accountManager.getUserSummary(userDetails.getUserId());
        return ResponseEntity.ok(mapper.toUserDto(user));
    }

    @Operation(
            summary = "Change own password",
            description = "Allows the authenticated user to change their password by providing the current one"
    )
    @IsAdminOrSurveyor
    @PatchMapping("/users/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        accountManager.changePassword(userDetails.getUserId(), request.oldPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Block company",
            description = "Deactivates a company by NIP. Only accessible by SUPER_ADMIN"
    )
    @IsSuperAdmin
    @PatchMapping("/{nip}/block")
    public ResponseEntity<Void> blockCompany(
            @PathVariable @Pattern(regexp = "\\d{10}") String nip
    ) {
        accountManager.blockCompany(nip);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Activate company",
            description = "Reactivates a blocked company by NIP. Only accessible by SUPER_ADMIN"
    )
    @IsSuperAdmin
    @PatchMapping("/{nip}/activate")
    public ResponseEntity<Void> activateCompany(
            @PathVariable @Pattern(regexp = "\\d{10}") String nip
    ) {
        accountManager.activateCompany(nip);
        return ResponseEntity.noContent().build();
    }
}
