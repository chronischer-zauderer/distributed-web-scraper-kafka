package com.scraperproducer.dataanalysis.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnalyticsDashboardEvent(LocalDateTime timestamp, List<AnalyticsPeriodSnapshot> periods) {
}
