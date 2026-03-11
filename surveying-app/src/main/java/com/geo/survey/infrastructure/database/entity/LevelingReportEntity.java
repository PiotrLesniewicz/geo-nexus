package com.geo.survey.infrastructure.database.entity;

import com.geo.survey.math.value.LevelingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "leveling_report")
public class LevelingReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "leveling_type" , nullable = false)
    @Enumerated(EnumType.STRING)
    private LevelingType levelingType;

    @Column(name = "start_height")
    private BigDecimal startHeight;

    @Column(name = "end_height")
    private BigDecimal endHeight;

    @Column(name = "measured_difference" , nullable = false)
    private BigDecimal measuredDifference;

    @Column(name = "theoretical_difference" , nullable = false)
    private BigDecimal theoreticalDifference;

    @Column(name = "misclosure" , nullable = false)
    private BigDecimal misclosure;

    @Column(name = "allowed_misclosure" , nullable = false)
    private BigDecimal allowedMisclosure;

    @Column(name = "is_within_tolerance" , nullable = false)
    private boolean isWithinTolerance;

    @Column(name = "sequence_distance")
    private BigDecimal sequenceDistance;

    @Column(name = "observation_time" , nullable = false)
    private OffsetDateTime observationTime;

    @Column(name = "generated_at" , nullable = false)
    private OffsetDateTime generatedAt;

    @ToString.Exclude
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stations" , columnDefinition = "jsonb" , nullable = false)
    private List<LevelingStationSnapshot> stations;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id" , nullable = false)
    private JobEntity job;
}
