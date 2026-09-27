package com.scraperproducer.dataanalysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraperproducer.dataanalysis.dto.AnalyticsDashboardEvent;
import com.scraperproducer.dataanalysis.dto.AnalyticsPeriodSnapshot;
import com.scraperproducer.dataanalysis.dto.MarketAnalyticsEvent;
import com.scraperproducer.dataanalysis.dto.MetricDto;
import com.scraperproducer.dataanalysis.dto.ProfileDemandDto;
import com.scraperproducer.dataanalysis.entity.ProcessedAnalyticsEvent;
import com.scraperproducer.dataanalysis.entity.ProfileMetric;
import com.scraperproducer.dataanalysis.entity.TechnologyMetric;
import com.scraperproducer.dataanalysis.repository.ProcessedAnalyticsEventRepository;
import com.scraperproducer.dataanalysis.repository.ProfileMetricRepository;
import com.scraperproducer.dataanalysis.repository.TechnologyMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TechnologyMetricRepository technologyMetricRepository;
    private final ProfileMetricRepository profileMetricRepository;
    private final ProcessedAnalyticsEventRepository processedEventRepository;
    private final ProfileClassifier profileClassifier;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(MarketAnalyticsEvent event) {
        validate(event);
        if (processedEventRepository.existsByJobId(event.jobId())) {
            // Kafka redelivery must not increment demand or salary aggregates twice.
            log.info("[ANALYSIS] Evento duplicado ignorado: jobId={}", event.jobId());
            return;
        }

        LocalDate extractionDate = event.scrapedAt() == null ? LocalDate.now() : event.scrapedAt().toLocalDate();
        // Store the same event in three independent calendar windows for simple dashboard queries.
        Map<String, LocalDate> periods = periodStarts(extractionDate);
        List<String> technologies = normalizeTechnologies(event.technologies());
        String profile = profileClassifier.classify(technologies);

        for (Map.Entry<String, LocalDate> period : periods.entrySet()) {
            for (String technology : technologies) {
                updateTechnologyMetric(period.getKey(), period.getValue(), technology, event.salary());
            }
            updateProfileMetric(period.getKey(), period.getValue(), profile);
        }

        ProcessedAnalyticsEvent processed = new ProcessedAnalyticsEvent();
        processed.setJobId(event.jobId());
        processed.setProcessedAt(LocalDateTime.now());
        processedEventRepository.save(processed);

        publishDashboardSnapshot(periods);
    }

    private void validate(MarketAnalyticsEvent event) {
        if (event == null || event.jobId() == null || event.jobId() <= 0) {
            throw new IllegalArgumentException("Evento market-analytics sin jobId válido");
        }
    }

    private Map<String, LocalDate> periodStarts(LocalDate date) {
        Map<String, LocalDate> periods = new LinkedHashMap<>();
        periods.put("DAILY", date);
        periods.put("WEEKLY", date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        periods.put("MONTHLY", date.withDayOfMonth(1));
        return periods;
    }

    private List<String> normalizeTechnologies(List<String> technologies) {
        if (technologies == null) {
            return List.of();
        }
        return technologies.stream()
                .filter(technology -> technology != null && !technology.isBlank())
                .map(technology -> technology.trim().replaceAll("\\s+", " "))
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(value -> value.toLowerCase(Locale.ROOT), value -> value,
                                (first, ignored) -> first, LinkedHashMap::new),
                        values -> new ArrayList<>(values.values())));
    }

    private void updateTechnologyMetric(String periodType, LocalDate periodStart, String technology, Double salary) {
        TechnologyMetric metric = technologyMetricRepository
                .findByPeriodTypeAndPeriodStartAndTechnology(periodType, periodStart, technology)
                .orElseGet(() -> newTechnologyMetric(periodType, periodStart, technology));
        metric.setJobCount(metric.getJobCount() + 1);
        if (validSalary(salary)) {
            metric.setSalarySum(metric.getSalarySum() + salary);
            metric.setSalaryCount(metric.getSalaryCount() + 1);
            metric.setAverageSalary(metric.getSalarySum() / metric.getSalaryCount());
        }
        technologyMetricRepository.save(metric);
    }

    private TechnologyMetric newTechnologyMetric(String periodType, LocalDate periodStart, String technology) {
        TechnologyMetric metric = new TechnologyMetric();
        metric.setPeriodType(periodType);
        metric.setPeriodStart(periodStart);
        metric.setTechnology(technology);
        return metric;
    }

    private void updateProfileMetric(String periodType, LocalDate periodStart, String profile) {
        ProfileMetric metric = profileMetricRepository
                .findByPeriodTypeAndPeriodStartAndProfile(periodType, periodStart, profile)
                .orElseGet(() -> newProfileMetric(periodType, periodStart, profile));
        metric.setJobCount(metric.getJobCount() + 1);
        profileMetricRepository.save(metric);
    }

    private ProfileMetric newProfileMetric(String periodType, LocalDate periodStart, String profile) {
        ProfileMetric metric = new ProfileMetric();
        metric.setPeriodType(periodType);
        metric.setPeriodStart(periodStart);
        metric.setProfile(profile);
        return metric;
    }

    private void publishDashboardSnapshot(Map<String, LocalDate> periods) {
        // Publish a self-contained snapshot so the future dashboard does not recalculate metrics.
        List<AnalyticsPeriodSnapshot> snapshots = periods.entrySet().stream()
                .map(period -> snapshot(period.getKey(), period.getValue()))
                .toList();
        AnalyticsDashboardEvent event = new AnalyticsDashboardEvent(LocalDateTime.now(), snapshots);
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("analytics-dashboard", event.timestamp().toString(), payload).get();
            log.info("[ANALYSIS] Snapshot publicado en analytics-dashboard");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar el snapshot", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación dashboard interrumpida", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Falló la publicación de analytics-dashboard", exception);
        }
    }

    private AnalyticsPeriodSnapshot snapshot(String periodType, LocalDate periodStart) {
        List<TechnologyMetric> metrics = technologyMetricRepository.findByPeriodTypeAndPeriodStart(periodType, periodStart);
        List<MetricDto> salaryByTechnology = metrics.stream()
                .filter(metric -> metric.getSalaryCount() > 0)
                .sorted(Comparator.comparing(TechnologyMetric::getTechnology))
                .map(metric -> new MetricDto(metric.getTechnology(), metric.getAverageSalary(), metric.getJobCount()))
                .toList();
        List<MetricDto> topPaid = metrics.stream()
                .filter(metric -> metric.getSalaryCount() > 0)
                .sorted(Comparator.comparingDouble(TechnologyMetric::getAverageSalary).reversed())
                .limit(5)
                .map(metric -> new MetricDto(metric.getTechnology(), metric.getAverageSalary(), metric.getJobCount()))
                .toList();
        List<MetricDto> demand = metrics.stream()
                .sorted(Comparator.comparingLong(TechnologyMetric::getJobCount).reversed())
                .map(metric -> new MetricDto(metric.getTechnology(), metric.getAverageSalary(), metric.getJobCount()))
                .toList();

        long backend = profileCount(periodType, periodStart, "BACKEND");
        long frontend = profileCount(periodType, periodStart, "FRONTEND");
        long other = profileCount(periodType, periodStart, "OTHER");
        return new AnalyticsPeriodSnapshot(periodType, periodStart, salaryByTechnology, topPaid, demand,
                new ProfileDemandDto(backend, frontend, other));
    }

    private long profileCount(String periodType, LocalDate periodStart, String profile) {
        return profileMetricRepository.findByPeriodTypeAndPeriodStartAndProfile(periodType, periodStart, profile)
                .map(ProfileMetric::getJobCount)
                .orElse(0L);
    }

    private boolean validSalary(Double salary) {
        return salary != null && Double.isFinite(salary) && salary > 0;
    }
}
