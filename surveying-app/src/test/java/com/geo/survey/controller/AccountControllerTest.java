package com.geo.survey.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geo.survey.api.controller.AccountController;
import com.geo.survey.api.dto.RegisterCompanyRequest;
import com.geo.survey.api.dto.RegisterUserRequest;
import com.geo.survey.api.mapper.AccountApiMapperImpl;
import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.domain.exception.ResourceNotFoundException;
import com.geo.survey.domain.model.Role;
import com.geo.survey.domain.service.AccountManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AccountController.class})
@Import(AccountApiMapperImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountManager accountManager;

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

        verify(accountManager).registerCompanyWithAdmin(any(), any(), eq("tajnehaslo"));
    }

    @Test
    void shouldReturn409_whenRegisterCompanyWithExistingNip() throws Exception {
        // given
        RegisterCompanyRequest request = getRegisterCompanyRequest();

        doThrow(new BusinessRuleViolationException("Company with nip [1234567890] already exists"))
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
        Long companyId = 1L;
        RegisterUserRequest request = new RegisterUserRequest(
                "anna@geo.pl", "Anna", "Nowak", Role.SURVEYOR, "haslo123"
        );

        doNothing().when(accountManager).registerUser(eq(companyId), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/{companyId}/users", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(accountManager).registerUser(eq(companyId), any(), eq("haslo123"));
    }

    @Test
    void shouldReturn404_whenRegisterUserToNonExistentCompany() throws Exception {
        // given
        Long companyId = 999L;
        RegisterUserRequest request = new RegisterUserRequest(
                "anna@geo.pl", "Anna", "Nowak", Role.SURVEYOR, "haslo123"
        );

        doThrow(new ResourceNotFoundException("Company with companyId [999] does not exist"))
                .when(accountManager).registerUser(eq(companyId), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/{companyId}/users", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409_whenRegisterUserToInactiveCompany() throws Exception {
        // given
        Long companyId = 3L;
        RegisterUserRequest request = new RegisterUserRequest(
                "anna@geo.pl", "Anna", "Nowak", Role.SURVEYOR, "haslo123"
        );

        doThrow(new BusinessRuleViolationException("Company is not active"))
                .when(accountManager).registerUser(eq(companyId), any(), any());

        // when, then
        mockMvc.perform(post("/api/v1/companies/{companyId}/users", companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // deleteUser

    @Test
    void shouldReturn204_whenDeleteUser() throws Exception {
        // given
        String email = "anna@geo.pl";
        doNothing().when(accountManager).deleteUser(email);

        // when, then
        mockMvc.perform(delete("/api/v1/companies/users/{email}", email))
                .andExpect(status().isNoContent());

        verify(accountManager).deleteUser(email);
    }

    @Test
    void shouldReturn404_whenDeleteNonExistentUser() throws Exception {
        // given
        String email = "nobody@nowhere.com";
        doThrow(new ResourceNotFoundException("User with email [nobody@nowhere.com] does not exist"))
                .when(accountManager).deleteUser(email);

        // when, then
        mockMvc.perform(delete("/api/v1/companies/users/{email}", email))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409_whenDeletingLastActiveAdmin() throws Exception {
        // given
        String email = "jan@geo.pl";
        doThrow(new BusinessRuleViolationException("Cannot delete the last active admin of the company"))
                .when(accountManager).deleteUser(email);

        // when, then
        mockMvc.perform(delete("/api/v1/companies/users/{email}", email))
                .andExpect(status().isConflict());
    }

    // blockCompany

    @Test
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
    void shouldReturn409_whenActivatingAlreadyActiveCompany() throws Exception {
        // given
        String nip = "1234567890";
        doThrow(new BusinessRuleViolationException("Company is already active"))
                .when(accountManager).activateCompany(nip);

        // when, then
        mockMvc.perform(patch("/api/v1/companies/{nip}/activate", nip))
                .andExpect(status().isConflict());
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
