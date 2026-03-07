package com.geo.survey.domain.service;

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
        validateNotNull(company, admin, password);
        Company savedCompany = companyService.save(company);
        User savedUser = userService.save(admin.withCompany(savedCompany).withRole(Role.ADMIN));
        userAuthService.save(savedUser, password);
    }

    @Transactional
    public void registerUser(Long companyId, User user, String password) {
        validateNotNull(companyId, user, password);
        Company company = companyService.findById(companyId);
        User savedUser = userService.save(user.withCompany(company));
        userAuthService.save(savedUser, password);
    }

    private void validateNotNull(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("Data registration must not be null!");
            }
        }
    }
}
