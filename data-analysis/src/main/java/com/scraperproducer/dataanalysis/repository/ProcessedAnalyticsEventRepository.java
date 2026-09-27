package com.scraperproducer.dataanalysis.repository;

import com.scraperproducer.dataanalysis.entity.ProcessedAnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedAnalyticsEventRepository extends JpaRepository<ProcessedAnalyticsEvent, Long> {
    boolean existsByJobId(Long jobId);
}
