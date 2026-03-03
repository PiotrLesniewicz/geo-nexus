package data;

import com.geo.survey.domain.model.Role;
import com.geo.survey.domain.model.User;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class UserFixture {

    public static final String DEFAULT_EMAIL = "jan.kowalski@geo.com";
    public static final String DEFAULT_NAME = "Jan";
    public static final String DEFAULT_SURNAME = "Kowalski";
    public static final Instant FIXED_INSTANT = Instant.parse("2020-10-03T12:00:00Z");
    public static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    public static User activeUser() {
        return User.builder()
                .email(DEFAULT_EMAIL)
                .name(DEFAULT_NAME)
                .surname(DEFAULT_SURNAME)
                .role(Role.SURVEYOR)
                .active(true)
                .registerAt(fixedDateTime())
                .company(CompanyFixture.activeCompany())
                .build();
    }

    public static User activeAdmin() {
        return User.builder()
                .email("admin@geo.com")
                .name("Admin")
                .surname("Kowalski")
                .role(Role.ADMIN)
                .active(true)
                .registerAt(fixedDateTime())
                .company(CompanyFixture.activeCompany())
                .build();
    }

    public static User blockedUser() {
        return User.builder()
                .email(DEFAULT_EMAIL)
                .name(DEFAULT_NAME)
                .surname(DEFAULT_SURNAME)
                .role(Role.SURVEYOR)
                .active(false)
                .registerAt(fixedDateTime())
                .deletedAt(fixedDateTime())
                .company(CompanyFixture.activeCompany())
                .build();
    }

    public static User userWithoutStatus() {
        return User.builder()
                .email(DEFAULT_EMAIL)
                .name(DEFAULT_NAME)
                .surname(DEFAULT_SURNAME)
                .role(Role.SURVEYOR)
                .company(CompanyFixture.activeCompany())
                .build();
    }

    public static OffsetDateTime fixedDateTime() {
        return OffsetDateTime.now(FIXED_CLOCK);
    }
}
