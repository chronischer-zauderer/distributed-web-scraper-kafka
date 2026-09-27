package com.scraperproducer.dataanalysis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "processed_analytics_events", uniqueConstraints = @UniqueConstraint(columnNames = "job_id"))
public class ProcessedAnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true)
    private Long jobId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
