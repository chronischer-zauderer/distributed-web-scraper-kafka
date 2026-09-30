package com.scraperproducer.analyticsdashboard.repository;

import com.scraperproducer.analyticsdashboard.entity.DashboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardSnapshotRepository extends JpaRepository<DashboardSnapshot, Long> {
}
