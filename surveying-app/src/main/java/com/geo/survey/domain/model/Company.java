package com.geo.survey.domain.model;

import com.geo.survey.domain.exception.ResourceActiveException;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.Clock;
import java.time.OffsetDateTime;

@With
@Value
@Builder(toBuilder = true)
public class Company {

    Long id;
    String name;
    String nip;
    Address address;
    boolean active;
    OffsetDateTime registerAt;
    OffsetDateTime blockedAt;

    public static Company register(String name, String nip, Address address, Clock clock) {
        return Company.builder()
                .name(name)
                .nip(nip)
                .address(address)
                .active(true)
                .registerAt(OffsetDateTime.now(clock))
                .build();
    }

    public Company activate() {
        if (this.active) {
            throw new ResourceActiveException("Company is already active");
        }
        return toBuilder()
                .active(true)
                .blockedAt(null)
                .build();
    }

    public Company block(Clock clock) {
        if (!this.active) {
            throw new ResourceActiveException("Company is already deactivated");
        }
        return toBuilder()
                .active(false)
                .blockedAt(OffsetDateTime.now(clock)).build();
    }
}
