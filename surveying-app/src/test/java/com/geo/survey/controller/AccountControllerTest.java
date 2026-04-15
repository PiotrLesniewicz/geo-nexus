package com.geo.survey.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geo.survey.api.controller.AccountController;
import com.geo.survey.api.dto.RegisterCompanyRequest;
import com.geo.survey.api.dto.RegisterUserRequest;
import com.geo.survey.api.mapper.AccountApiMapperImpl;
import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Role;
import com.geo.survey.domain.model.User;
import com.geo.survey.domain.model.UserSummary;
import com.geo.survey.domain.service.AccountManager;
import com.geo.survey.infrastructure.security.CustomUserDetails;
import com.geo.survey.infrastructure.security.CustomUserDetailsService;
import com.geo.survey.infrastructure.security.JwtService;
import com.geo.survey.testconfig.SecurityTestConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AccountController.class})
@Import({
        AccountApiMapperImpl.class,
        SecurityTestConfiguration.class
})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountManager accountManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    // registerCompanyWithAdmin

    @Test
    void shouldReturn201_whenRegisterCompanyWithAdmin() throws Exception {
        // given
        RegisterCompanyRequest request = getRegisterCompanyRequest();

        doNothing().when(accountManager).registerCompanyWithAdmin(any(), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(accountManager).registerCompanyWithAdmin(any(), any(), any());
    }

    @Test
    void shouldReturn409_whenRegisterCompanyWithExistingNip() throws Exception {
        // given
        RegisterCompanyRequest request = getRegisterCompanyRequest();

        doThrow(new BusinessRuleViolationException("Company with nip already exists"))
                .when(accountManager).registerCompanyWithAdmin(any(), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // registerUser

    @Test
    void shouldReturn201_whenRegisterUser() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        RegisterUserRequest request = newRegisterUser();

        doNothing().when(accountManager).registerUser(any(), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/users")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(accountManager).registerUser(any(), any(), any());
    }

    @Test
    void shouldReturn404_whenRegisterUserToNonExistentCompany() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        RegisterUserRequest request = newRegisterUser();
        Long companyId = userDetails.getCompanyId();

        doThrow(new ResourceNotFoundException("Company with companyId [%s] does not exist".formatted(companyId)))
                .when(accountManager).registerUser(eq(companyId), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/users")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409_whenRegisterUserToInactiveCompany() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        RegisterUserRequest request = newRegisterUser();

        doThrow(new BusinessRuleViolationException("Company is not active"))
                .when(accountManager).registerUser(any(), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/users")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // deleteUser

    @Test
    void shouldReturn204_whenDeleteUser() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        String email = "anna@geo.pl";
        doNothing().when(accountManager).deleteUser(userDetails.getCompanyId(), email);

        // when, then
        mockMvc.perform(delete("/api/v1/companies/users/{email}", email)
                        .with(user(userDetails)))
                .andExpect(status().isNoContent());

        verify(accountManager).deleteUser(userDetails.getCompanyId(), email);
    }

    @Test
    void shouldReturn404_whenDeleteNonExistentUser() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        String email = "nobody@nowhere.com";
        doThrow(new ResourceNotFoundException("User with email [%s] does not exist".formatted(email)))
                .when(accountManager).deleteUser(any(), eq(email));

        // when, then
        mockMvc.perform(delete("/api/v1/companies/users/{email}", email)
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409_whenDeletingLastActiveAdmin() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        String email = "jan@geo.pl";
        doThrow(new BusinessRuleViolationException("Cannot delete the last active admin of the company"))
                .when(accountManager).deleteUser(any(), any());

        // when, then
        mockMvc.perform(delete("/api/v1/companies/users/{email}", email)
                        .with(user(userDetails)))
                .andExpect(status().isConflict());
    }

    // blockCompany

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldReturn204_whenBlockCompany() throws Exception {
        // given
        String nip = "1234567890";
        doNothing().when(accountManager).blockCompany(nip);

        // when, then
        mockMvc.perform(patch("/api/v1/companies/{nip}/block", nip))
                .andExpect(status().isNoContent());

        verify(accountManager).blockCompany(nip);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldReturn409_whenBlockingAlreadyInactiveCompany() throws Exception {
        // given
        String nip = "1234567890";
        doThrow(new BusinessRuleViolationException("Company is already deactivated"))
                .when(accountManager).blockCompany(nip);

        // when, then
        mockMvc.perform(patch("/api/v1/companies/{nip}/block", nip))
                .andExpect(status().isConflict());
    }

    // activateCompany

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldReturn204_whenActivateCompany() throws Exception {
        // given
        String nip = "1122334455";
        doNothing().when(accountManager).activateCompany(nip);

        // when, then
        mockMvc.perform(patch("/api/v1/companies/{nip}/activate", nip))
                .andExpect(status().isNoContent());

        verify(accountManager).activateCompany(nip);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldReturn409_whenActivatingAlreadyActiveCompany() throws Exception {
        // given
        String nip = "1234567890";
        doThrow(new BusinessRuleViolationException("Company is already active"))
                .when(accountManager).activateCompany(nip);

        // when, then
        mockMvc.perform(patch("/api/v1/companies/{nip}/activate", nip))
                .andExpect(status().isConflict());
    }

    // getUser by email

    @Test
    void shouldReturn200_whenAdminGetsUserByEmail() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        String email = "anna@geo.pl";

        when(accountManager.getUserSummary(userDetails.getCompanyId(), email))
                .thenReturn(getUserSummary());

        // when, then
        mockMvc.perform(get("/api/v1/companies/users/{email}", email)
                        .with(user(userDetails)))
                .andExpect(status().isOk());

        verify(accountManager).getUserSummary(userDetails.getCompanyId(), email);
    }

    @Test
    void shouldReturn404_whenAdminGetsNonExistentUser() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();
        String email = "nobody@geo.pl";

        doThrow(new ResourceNotFoundException("User with email [%s] does not exist".formatted(email)))
                .when(accountManager).getUserSummary(userDetails.getCompanyId(), email);

        // when, then
        mockMvc.perform(get("/api/v1/companies/users/{email}", email)
                        .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403_whenSurveyorTriesToGetUserByEmail() throws Exception {
        // given
        CustomUserDetails userDetails = getSurveyorUserDetails();
        String email = "anna@geo.pl";

        // when, then
        mockMvc.perform(get("/api/v1/companies/users/{email}", email)
                        .with(user(userDetails)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(accountManager);
    }

// getUser me

    @Test
    void shouldReturn200_whenAdminGetsOwnProfile() throws Exception {
        // given
        CustomUserDetails userDetails = getAdminUserDetails();

        when(accountManager.getUserSummary(userDetails.getUserId()))
                .thenReturn(getUserSummary());

        // when, then
        mockMvc.perform(get("/api/v1/companies/users/me")
                        .with(user(userDetails)))
                .andExpect(status().isOk());

        verify(accountManager).getUserSummary(userDetails.getUserId());
    }

    @Test
    void shouldReturn200_whenSurveyorGetsOwnProfile() throws Exception {
        // given
        CustomUserDetails userDetails = getSurveyorUserDetails();

        when(accountManager.getUserSummary(userDetails.getUserId()))
                .thenReturn(getUserSummary());

        // when, then
        mockMvc.perform(get("/api/v1/companies/users/me")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

// helpers

    private static CustomUserDetails getSurveyorUserDetails() {
        return new CustomUserDetails(
                2L,
                1L,
                "surveyor",
                "password",
                Role.SURVEYOR,
                true,
                true,
                false
        );
    }

    private static UserSummary getUserSummary() {
        User user = User.builder()
                .id(2L)
                .email("anna@geo.pl")
                .name("Anna")
                .surname("Nowak")
                .role(Role.SURVEYOR)
                .active(true)
                .registerAt(OffsetDateTime.now())
                .deletedAt(null)
                .build();

        return UserSummary.builder()
                .user(user)
                .countJob(5)
                .openJob(2)
                .build();
    }

    private static @NotNull CustomUserDetails getAdminUserDetails() {
        return new CustomUserDetails(
                1L,
                1L,
                "admin",
                "password",
                Role.ADMIN,
                true,
                true,
                false
        );
    }

    private static @NotNull RegisterUserRequest newRegisterUser() {
        return new RegisterUserRequest(
                "anna@geo.pl", "Anna", "Nowak", Role.SURVEYOR, "haslo123"
        );
    }


    private static @NotNull RegisterCompanyRequest getRegisterCompanyRequest() {
        return RegisterCompanyRequest.builder()
                .companyName("GeoSurvey")
                .nip("1234567890")
                .street("Miernicza")
                .buildingNumber("12")
                .postalCode("00-001")
                .city("Warszawa")
                .country("Polska")
                .adminEmail("jan@geo.pl")
                .adminName("Jan")
                .adminSurname("Kowalski")
                .password("tajnehaslo")
                .build();
    }
}
