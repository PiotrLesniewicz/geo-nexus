package data;

import com.geo.survey.domain.model.Address;
import com.geo.survey.domain.model.Company;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class CompanyFixture {

    public static final String DEFAULT_NIP = "1234567890";
    public static final String DEFAULT_NAME = "Geo Nexus";
    public static final Instant FIXED_INSTANT = Instant.parse("2020-10-03T12:00:00Z");
    public static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    public static Company activeCompany() {
        return Company.builder()
                .name(DEFAULT_NAME)
                .nip(DEFAULT_NIP)
                .active(true)
                .build();
    }

    public static Company blockedCompany() {
        return Company.builder()
                .name(DEFAULT_NAME)
                .nip(DEFAULT_NIP)
                .active(false)
                .blockedAt(OffsetDateTime.now(FIXED_CLOCK))
                .build();
    }

    public static Company companyWithoutStatus() {
        return Company.builder()
                .name(DEFAULT_NAME)
                .nip(DEFAULT_NIP)
                .build();
    }

    public static Company companyWithAddress() {
        return Company.builder()
                .name(DEFAULT_NAME)
                .nip(DEFAULT_NIP)
                .address(defaultAddress())
                .build();
    }

    public static Address defaultAddress() {
        return Address.builder()
                .city("Piaseczno")
                .street("Warszawska")
                .buildingNumber("23")
                .apartmentNumber("7A")
                .country("Polska")
                .build();
    }

    public static OffsetDateTime fixedDateTime() {
        return OffsetDateTime.now(FIXED_CLOCK);
    }
}
