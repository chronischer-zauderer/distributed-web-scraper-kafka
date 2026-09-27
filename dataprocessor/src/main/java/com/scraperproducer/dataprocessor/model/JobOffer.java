package com.scraperproducer.dataprocessor.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_offers")
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String company;

    private Double salary;

    @Column(name = "required_skills")
    private String requiredSkills;

    private String location;

    @Column(name = "source_url", length = 1024)
    private String sourceUrl;

    @Column(name = "dedupe_key", unique = true, length = 64)
    private String dedupeKey;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    @PrePersist
    protected void onCreate() {
        this.extractedAt = LocalDateTime.now();
    }
}
