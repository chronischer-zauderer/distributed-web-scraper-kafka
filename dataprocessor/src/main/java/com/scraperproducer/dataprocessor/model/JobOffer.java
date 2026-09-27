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
    private String title;          // Ej: "Backend Developer Java"

    private String company;        // Ej: "Perficient"

    private Double salary;         // Rango numérico limpio para estadísticas

    @Column(name = "required_skills")
    private String requiredSkills; // Tecnologías concatenadas (Ej: "Java, Spring, SQL")

    private String location;

    @Column(name = "source_url", length = 1024)
    private String sourceUrl;      // URL de auditoría

    @Column(name = "dedupe_key", unique = true, length = 64)
    private String dedupeKey;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    @PrePersist
    protected void onCreate() {
        this.extractedAt = LocalDateTime.now();
    }
}
