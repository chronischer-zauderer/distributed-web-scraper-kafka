package com.scraperproducer.analyticsdashboard.controller;

import com.scraperproducer.analyticsdashboard.dto.AnalyticsDashboardEvent;
import com.scraperproducer.analyticsdashboard.service.DashboardSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardSnapshotService snapshotService;

    @GetMapping
    public ResponseEntity<AnalyticsDashboardEvent> latestSnapshot() {
        return snapshotService.latestSnapshot()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
