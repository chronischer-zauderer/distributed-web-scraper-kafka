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
@Table(name = "technology_metrics", uniqueConstraints = @UniqueConstraint(columnNames = {"period_type", "period_start", "technology"}))
public class TechnologyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_type", nullable = false, length = 16)
    private String periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false, length = 80)
    private String technology;

    @Column(name = "job_count", nullable = false)
    private long jobCount;

    @Column(name = "salary_sum", nullable = false)
    private double salarySum;

    @Column(name = "salary_count", nullable = false)
    private long salaryCount;

    @Column(name = "average_salary", nullable = false)
    private double averageSalary;
}
