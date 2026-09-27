package com.scraperproducer.dataanalysis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profile_metrics", uniqueConstraints = @UniqueConstraint(columnNames = {"period_type", "period_start", "profile"}))
public class ProfileMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_type", nullable = false, length = 16)
    private String periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false, length = 16)
    private String profile;

    @Column(name = "job_count", nullable = false)
    private long jobCount;
}
