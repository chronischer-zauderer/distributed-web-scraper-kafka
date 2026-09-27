package com.scraperproducer.dataanalysis.repository;

import com.scraperproducer.dataanalysis.entity.TechnologyMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TechnologyMetricRepository extends JpaRepository<TechnologyMetric, Long> {
    Optional<TechnologyMetric> findByPeriodTypeAndPeriodStartAndTechnology(String periodType, LocalDate periodStart, String technology);
    List<TechnologyMetric> findByPeriodTypeAndPeriodStart(String periodType, LocalDate periodStart);
}
