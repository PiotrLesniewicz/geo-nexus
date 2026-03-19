package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.model.Company;
import com.geo.survey.domain.model.Role;
import com.geo.survey.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AccountManager {

    private final CompanyService companyService;
    private final UserService userService;
    private final UserAuthService userAuthService;

    @Transactional
    public void registerCompanyWithAdmin(Company company, User admin, String password) {
        Company savedCompany = companyService.save(company);
        User savedUser = userService.save(admin.withCompany(savedCompany).withRole(Role.ADMIN));
        userAuthService.save(savedUser, password);
    }

    @Transactional
    public void registerUser(Long companyId, User user, String password) {
        Company company = companyService.findById(companyId);
        if (!company.isActive()) {
            throw new BusinessRuleViolationException("Company is not active");
        }
        User savedUser = userService.save(user.withCompany(company));
        userAuthService.save(savedUser, password);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userService.findByEmail(email);
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

    private boolean isLastActiveAdmin(User user) {
        return userService.countActiveAdmins(user.getCompany().getId()) <= 1;
    }
}
