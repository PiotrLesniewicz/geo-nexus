package com.geo.survey.infrastructure.database.entity;

import com.geo.survey.domain.model.Role;
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
@Table(name = "user_account")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email" , nullable = false, unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "role" , nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "active" , nullable = false)
    private boolean active;

    @Column(name = "register_at" , nullable = false)
    private OffsetDateTime registerAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id" , nullable = false)
    private CompanyEntity company;
}
