package com.scraperproducer.analyticsdashboard.dto;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsPeriodSnapshot(String periodType, LocalDate periodStart,
                                      List<MetricDto> salaryByTechnology,
                                      List<MetricDto> topPaidTechnologies,
                                      List<MetricDto> technologyDemand,
                                      ProfileDemandDto profileDemand) {
}
