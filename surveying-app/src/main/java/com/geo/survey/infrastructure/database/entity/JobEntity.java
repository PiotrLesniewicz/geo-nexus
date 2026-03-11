package com.geo.survey.infrastructure.database.entity;

import com.geo.survey.domain.model.StatusJob;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "job")
public class JobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "job_identifier" , nullable = false)
    private String jobIdentifier;

    @Embedded
    private AddressEntity address;

    @Column(name = "status" , nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusJob status;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at" , nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id" , nullable = false)
    private CompanyEntity company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id" , nullable = false)
    private UserEntity user;
}
