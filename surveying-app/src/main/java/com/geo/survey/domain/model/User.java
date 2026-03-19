package com.geo.survey.domain.model;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Clock;
import java.time.OffsetDateTime;

@With
@Value
@Builder(toBuilder = true)
public class User {

    Long id;
    String email;
    String name;
    String surname;
    Role role;
    boolean active;
    OffsetDateTime registerAt;
    OffsetDateTime deletedAt;
    Company company;

    public static User register(String email, String name, String surname, Role role, Company company, Clock clock) {
        return User.builder()
                .email(email)
                .name(name)
                .surname(surname)
                .role(role)
                .active(true)
                .registerAt(OffsetDateTime.now(clock))
                .company(company)
                .build();
    }

    public User delete(Clock clock) {
        if (!this.active) {
            throw new BusinessRuleViolationException("User is already deleted: [%s]".formatted(email));
        }
        return toBuilder()
                .active(false)
                .deletedAt(OffsetDateTime.now(clock))
                .build();
    }

    public User changeRole(Role newRole) {
        if (this.role == newRole) {
            throw new BusinessRuleViolationException("User [%s] already has role [%s]".formatted(email, newRole));
        }
        return toBuilder()
                .role(newRole)
                .build();
    }
}
