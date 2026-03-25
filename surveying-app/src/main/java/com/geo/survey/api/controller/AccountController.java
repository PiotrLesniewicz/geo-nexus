package com.geo.survey.api.controller;

import com.geo.survey.api.dto.RegisterCompanyRequest;
import com.geo.survey.api.dto.RegisterUserRequest;
import com.geo.survey.api.mapper.AccountApiMapper;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.service.AccountManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class AccountController {

    private final AccountManager accountManager;
    private final AccountApiMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<Void> registerCompanyWithAdmin(@RequestBody RegisterCompanyRequest request) {
        Company company = mapper.toCompany(request);
        User admin = mapper.toAdminUser(request);
        accountManager.registerCompanyWithAdmin(company, admin, request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{companyId}/users")
    public ResponseEntity<Void> registerUser(
            @PathVariable Long companyId,
            @RequestBody RegisterUserRequest request) {
        User user = mapper.toUser(request);
        accountManager.registerUser(companyId, user, request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/users/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        accountManager.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{nip}/block")
    public ResponseEntity<Void> blockCompany(@PathVariable String nip) {
        accountManager.blockCompany(nip);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{nip}/activate")
    public ResponseEntity<Void> activateCompany(@PathVariable String nip) {
        accountManager.activateCompany(nip);
        return ResponseEntity.noContent().build();
    }
}
