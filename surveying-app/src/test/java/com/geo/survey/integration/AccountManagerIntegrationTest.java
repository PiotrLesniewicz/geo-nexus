package com.geo.survey.integration;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.*;
import com.geo.survey.domain.service.AccountManager;
import com.geo.survey.domain.service.CompanyService;
import com.geo.survey.domain.service.UserAuthService;
import com.geo.survey.domain.service.UserService;
import com.geo.survey.testconfig.TestContainerConfig;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AllArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/db/cleanup.sql", "/db/test_data_account.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AccountManagerIntegrationTest extends TestContainerConfig {

    private AccountManager accountManager;
    private CompanyService companyService;
    private UserService userService;
    private UserAuthService userAuthService;
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private Clock clock;

    private static final Company DEFAULT_COMPANY = Company.builder()
            .name("Test Company")
            .nip("777-777-777")
            .build();

    private static final User DEFAULT_USER = User.builder()
            .email("kovalsky@gmail.com")
            .build();

    private static final String DEFAULT_PASSWORD = "supertajnehaslo";

    @BeforeEach
    void setUp() {
        Instant fixInstant = Instant.parse("2020-10-03T12:00:00Z");
        Mockito.when(clock.instant()).thenReturn(fixInstant);
        Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    // tests for create company account
    @Test
    void shouldCreateCompanyWithCorrectData() {
        //given
        OffsetDateTime registerDate = OffsetDateTime.now(clock);
        Address address = Address.builder()
                .city("Piaseczno")
                .street("Warszawska")
                .buildingNumber("23")
                .apartmentNumber("7A")
                .country("Polska")
                .build();

        Company company = DEFAULT_COMPANY.toBuilder()
                .address(address)
                .build();

        User admin = DEFAULT_USER;

        String password = DEFAULT_PASSWORD;

        //when
        accountManager.registerCompanyWithAdmin(company, admin, password);

        //then
        Company savedCompany = companyService.findByNip(company.getNip());
        User savedUser = userService.findByEmail(admin.getEmail());
        UserAuth savedAuth = userAuthService.findByUserId(savedUser.getId());

        assertThat(savedCompany.getId()).isNotNull();
        assertThat(savedCompany)
                .returns(company.getNip(), Company::getNip)
                .returns(registerDate, Company::getRegisterAt)
                .returns(true, Company::isActive);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser)
                .returns(admin.getEmail(), User::getEmail)
                .returns(Role.ADMIN, User::getRole)
                .returns(true, User::isActive)
                .returns(registerDate, User::getRegisterAt);

        assertThat(savedAuth.getId()).isNotNull();
        assertThat(passwordEncoder.matches(password, savedAuth.getPasswordHash())).isTrue();
    }

    @Test
    void shouldThrowException_WhenNipCompanyAlreadyExistInDB() {
        //given
        Company company = DEFAULT_COMPANY.toBuilder()
                .nip("1234567890") //nip with 'test_data_account.sql'
                .build();

        //when then
        assertThatThrownBy(() -> accountManager.registerCompanyWithAdmin(company, DEFAULT_USER, DEFAULT_PASSWORD))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining(company.getNip());
    }

    @Test
    void shouldThrowException_WhenEmailAlreadyExistInDB() {
        //given
        User admin = DEFAULT_USER.toBuilder()
                .email("jan.kowalski@geosurvey.pl") //email with 'test_data_account.sql'
                .build();

        //when then
        assertThatThrownBy(() -> accountManager.registerCompanyWithAdmin(DEFAULT_COMPANY, admin, DEFAULT_PASSWORD))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining(admin.getEmail());
    }

    // tests for register user to existing company
    @Test
    void shouldRegisterUserToExistingCompany() {
        //given
        Long companyId = 1L; // from test_data_account.sql

        User user = DEFAULT_USER.toBuilder()
                .role(Role.TECHNIC)
                .build();

        //when
        accountManager.registerUser(companyId, user, DEFAULT_PASSWORD);

        //then
        User savedUser = userService.findByEmail(user.getEmail());
        UserAuth savedAuth = userAuthService.findByUserId(savedUser.getId());

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser)
                .returns(user.getEmail(), User::getEmail)
                .returns(Role.TECHNIC, User::getRole)
                .returns(true, User::isActive)
                .returns(OffsetDateTime.now(clock), User::getRegisterAt)
                .returns(companyId, u -> u.getCompany().getId());

        assertThat(savedAuth.getId()).isNotNull();
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, savedAuth.getPasswordHash())).isTrue();
    }

    @Test
    void shouldThrowException_WhenCompanyNotFound() {
        //given
        Long nonExistentCompanyId = 999L;

        //when then
        assertThatThrownBy(() -> accountManager.registerUser(nonExistentCompanyId, DEFAULT_USER, DEFAULT_PASSWORD))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(nonExistentCompanyId));
    }

    @Test
    void shouldThrowException_WhenEmailAlreadyExistsInCompany() {
        //given
        Long companyId = 1L; // from test_data_account.sql

        User user = DEFAULT_USER.toBuilder()
                .email("jan.kowalski@geosurvey.pl") // email from test_data_account.sql
                .build();

        //when then
        assertThatThrownBy(() -> accountManager.registerUser(companyId, user, DEFAULT_PASSWORD))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining(user.getEmail());
    }

    @Test
    void shouldThrowException_WhenCompanyIsNotActive() {
        //given
        Long blockedCompanyId = 3L; // TerraMap — blocked from test_data_account.sql

        //when then
        assertThatThrownBy(() -> accountManager.registerUser(blockedCompanyId, DEFAULT_USER, DEFAULT_PASSWORD))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Company is not active");
    }

    // tests for delete user
    @Test
    void shouldSoftDeleteNonAdminUser() {
        // given
        String email = "anna.nowak@geosurvey.pl"; // SURVEYOR from test_data_account.sql

        // when
        accountManager.deleteUser(email);

        // then
        User deleted = userService.findByEmail(email);
        assertThat(deleted.isActive()).isFalse();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    void shouldThrowException_WhenDeletingLastActiveAdmin() {
        // given — jan.kowalski is the only active ADMIN in company 1
        String email = "jan.kowalski@geosurvey.pl";

        // when then
        assertThatThrownBy(() -> accountManager.deleteUser(email))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowException_WhenUserNotFound() {
        // given
        String nonExistentEmail = "nobody@nowhere.com";

        // when then
        assertThatThrownBy(() -> accountManager.deleteUser(nonExistentEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistentEmail);
    }

    @Test
    void shouldDeleteAdminUserWhenNotTheLastActiveAdmin() {
        // given — add another admin first so we can delete one
        Long companyId = 1L;
        User secondAdmin = User.builder()
                .email("second.admin@geosurvey.pl")
                .name("Second")
                .surname("Admin")
                .role(Role.ADMIN)
                .build();
        accountManager.registerUser(companyId, secondAdmin, DEFAULT_PASSWORD);

        // when
        accountManager.deleteUser(secondAdmin.getEmail());

        // then
        User deleted = userService.findByEmail(secondAdmin.getEmail());
        assertThat(deleted.isActive()).isFalse();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    // tests for block and activate company
    @Test
    void shouldBlockActiveCompany() {
        // given
        String nip = "1234567890"; // GeoSurvey — active from test_data_account.sql

        // when
        accountManager.blockCompany(nip);

        // then
        Company blocked = companyService.findByNip(nip);
        assertThat(blocked.isActive()).isFalse();
        assertThat(blocked.getBlockedAt()).isNotNull();
    }

    @Test
    void shouldActivateBlockedCompany() {
        // given
        String nip = "1122334455"; // TerraMap — blocked from test_data_account.sql

        // when
        accountManager.activateCompany(nip);

        // then
        Company activated = companyService.findByNip(nip);
        assertThat(activated.isActive()).isTrue();
        assertThat(activated.getBlockedAt()).isNull();
    }

    @Test
    void shouldThrowException_WhenBlockingAlreadyInactiveCompany() {
        // given
        String nip = "1122334455"; // TerraMap — already blocked from test_data_account.sql

        // when then
        assertThatThrownBy(() -> accountManager.blockCompany(nip))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Company is already inactive");
    }

    @Test
    void shouldThrowException_WhenActivatingAlreadyActiveCompany() {
        // given
        String nip = "1234567890"; // GeoSurvey — active from test_data_account.sql

        // when then
        assertThatThrownBy(() -> accountManager.activateCompany(nip))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Company is already active");
    }

    @Test
    void shouldThrowException_WhenBlockingNonExistentCompany() {
        // given
        String nonExistentNip = "9999999999";

        // when then
        assertThatThrownBy(() -> accountManager.blockCompany(nonExistentNip))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistentNip);
    }

    @Test
    void shouldThrowException_WhenActivatingNonExistentCompany() {
        // given
        String nonExistentNip = "9999999999";

        // when then
        assertThatThrownBy(() -> accountManager.activateCompany(nonExistentNip))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(nonExistentNip);
    }

}
