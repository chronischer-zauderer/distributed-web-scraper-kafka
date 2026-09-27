package com.scraperproducer.dataanalysis.repository;

import com.scraperproducer.dataanalysis.entity.ProfileMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProfileMetricRepository extends JpaRepository<ProfileMetric, Long> {
    Optional<ProfileMetric> findByPeriodTypeAndPeriodStartAndProfile(String periodType, LocalDate periodStart, String profile);
    List<ProfileMetric> findByPeriodTypeAndPeriodStart(String periodType, LocalDate periodStart);
}
