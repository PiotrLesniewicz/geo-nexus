package com.geo.survey.infrastructure.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString(of = {"name" , "nip"})
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "company")
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name" , nullable = false)
    private String name;

    @Column(name = "nip" , unique = true)
    private String nip;

    @Embedded
    private AddressEntity address;

    @Column(name = "active" , nullable = false)
    private boolean active;

    @Column(name = "register_at" , nullable = false)
    private OffsetDateTime registerAt;

    @Column(name = "blocked_at")
    private OffsetDateTime blockedAt;
}
