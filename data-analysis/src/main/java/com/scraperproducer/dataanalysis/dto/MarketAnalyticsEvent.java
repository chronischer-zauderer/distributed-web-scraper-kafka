package com.scraperproducer.dataanalysis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MarketAnalyticsEvent(
        @JsonAlias("id") Long jobId,
        @JsonAlias("puesto") String title,
        @JsonAlias("empresa") String company,
        @JsonAlias("salario") Double salary,
        List<String> technologies,
        String location,
        String sourceUrl,
        LocalDateTime scrapedAt
) {
}
