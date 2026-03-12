package data;

import com.geo.survey.domain.model.Address;
import com.geo.survey.domain.model.Job;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class JobFixture {

    public static final String DEFAULT_JOB_IDENTIFIER = "JOB-2024-001";
    public static final String DEFAULT_DESCRIPTION = "Surveying project for building foundation";
    public static final Instant FIXED_INSTANT = Instant.parse("2020-10-03T12:00:00Z");
    public static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    public static Job jobWithoutStatus() {
        return Job.builder()
                .jobIdentifier(DEFAULT_JOB_IDENTIFIER)
                .address(defaultAddress())
                .description(DEFAULT_DESCRIPTION)
                .company(CompanyFixture.activeCompanyWithId())
                .user(UserFixture.activeUser())
                .build();
    }

    public static Address defaultAddress() {
        return Address.builder()
                .city("Warsaw")
                .street("Main Street")
                .buildingNumber("42")
                .apartmentNumber("5")
                .country("Poland")
                .build();
    }

    public static OffsetDateTime fixedDateTime() {
        return OffsetDateTime.now(FIXED_CLOCK);
    }
}


