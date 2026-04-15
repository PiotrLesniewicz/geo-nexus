package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.UnauthorizedAccessException;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.Role;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.model.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AccountManager {

    private final CompanyService companyService;
    private final UserService userService;
    private final UserAuthService userAuthService;
    private final JobService jobService;

    @Transactional
    public void registerCompanyWithAdmin(Company company, User admin, String password) {
        Company savedCompany = companyService.save(company);
        User savedUser = userService.save(admin.withCompany(savedCompany).withRole(Role.ADMIN));
        userAuthService.save(savedUser, password);
    }

    @Transactional
    public void registerUser(Long companyId, User user, String password) {
        assertUserIsNotSuperAdmin(user);
        Company company = companyService.findById(companyId);
        if (!company.isActive()) {
            throw new BusinessRuleViolationException("Company is not active");
        }
        User savedUser = userService.save(user.withCompany(company));
        userAuthService.save(savedUser, password);
    }

    @Transactional
    public void deleteUser(Long companyId, String email) {
        User user = userService.findByEmail(email, companyId);
        assertUserIsNotSuperAdmin(user);
        if (!Objects.equals(user.getCompany().getId(), companyId)) {
            throw new UnauthorizedAccessException("User does not belong to the company");
        }
        if (user.getRole() == Role.ADMIN && isLastActiveAdmin(user)) {
            throw new BusinessRuleViolationException(
                    "Cannot delete the last active admin of the company"
            );
        }
        userService.deleteUser(user);
    }

    @Transactional
    public void blockCompany(String nip) {
        Company company = companyService.findByNip(nip);
        if (!company.isActive()) {
            throw new BusinessRuleViolationException("Company is already inactive");
        }
        companyService.blockCompany(company);
    }

    @Transactional
    public void activateCompany(String nip) {
        Company company = companyService.findByNip(nip);
        if (company.isActive()) {
            throw new BusinessRuleViolationException("Company is already active");
        }
        companyService.activateCompany(company);
    }

    @Transactional
    public UserSummary getUserSummary(Long userId) {
        User user = userService.findById(userId);
        int countJob = jobService.countByUserId(user.getId());
        int openJob  = jobService.countOpenByUserId(user.getId());
        return buildUserSummary(user, countJob, openJob);
    }

    @Transactional
    public UserSummary getUserSummary(Long companyId, String email) {
        User user = userService.findByEmail(email, companyId);
        int countJob = jobService.countByUserId(user.getId());
        int openJob  = jobService.countOpenByUserId(user.getId());
        return buildUserSummary(user, countJob, openJob);
    }

    private static void assertUserIsNotSuperAdmin(User user) {
        if (user.getRole() == Role.SUPER_ADMIN) {
            throw new BusinessRuleViolationException("Cannot assign SUPER_ADMIN role");
        }
    }

    private boolean isLastActiveAdmin(User user) {
        return userService.countActiveAdmins(user.getCompany().getId()) <= 1;
    }

    private static UserSummary buildUserSummary(User user, int countJob, int openJob) {
        return UserSummary.builder()
                .user(user)
                .countJob(countJob)
                .openJob(openJob)
                .build();
    }
}
