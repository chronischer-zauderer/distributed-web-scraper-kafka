package com.scraperproducer.dataprocessor.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MarketAnalyticsEvent(
        Long jobId,
        String title,
        String company,
        Double salary,
        List<String> technologies,
        String location,
        String sourceUrl,
        LocalDateTime scrapedAt
) {
}
