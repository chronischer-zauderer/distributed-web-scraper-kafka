package com.scraperproducer.dataanalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraperproducer.dataanalysis.dto.MarketAnalyticsEvent;
import com.scraperproducer.dataanalysis.entity.TechnologyMetric;
import com.scraperproducer.dataanalysis.repository.ProcessedAnalyticsEventRepository;
import com.scraperproducer.dataanalysis.repository.ProfileMetricRepository;
import com.scraperproducer.dataanalysis.repository.TechnologyMetricRepository;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AnalyticsServiceTest {

    @Test
    void aggregatesTechnologySalaryAndPublishesDashboardSnapshot() {
        TechnologyMetricRepository technologyRepository = mock(TechnologyMetricRepository.class);
        ProfileMetricRepository profileRepository = mock(ProfileMetricRepository.class);
        ProcessedAnalyticsEventRepository processedRepository = mock(ProcessedAnalyticsEventRepository.class);
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        AnalyticsService service = new AnalyticsService(technologyRepository, profileRepository,
                processedRepository, new ProfileClassifier(), kafkaTemplate,
                new ObjectMapper().findAndRegisterModules());

        when(processedRepository.existsByJobId(10L)).thenReturn(false);
        when(technologyRepository.findByPeriodTypeAndPeriodStartAndTechnology(anyString(), any(), eq("Java")))
                .thenReturn(Optional.empty());
        when(technologyRepository.findByPeriodTypeAndPeriodStart(anyString(), any())).thenReturn(java.util.List.of());
        when(profileRepository.findByPeriodTypeAndPeriodStartAndProfile(anyString(), any(), anyString()))
                .thenReturn(Optional.empty());
        when(kafkaTemplate.send(eq("analytics-dashboard"), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        service.process(new MarketAnalyticsEvent(10L, "Java Developer", "Acme", 5000000.0,
                java.util.List.of("Java", "Java"), "Cali", "https://example.test/10",
                LocalDateTime.of(2026, 9, 26, 10, 0)));

        verify(technologyRepository, times(3)).save(any(TechnologyMetric.class));
        verify(processedRepository).save(any());
        verify(kafkaTemplate).send(eq("analytics-dashboard"), anyString(), anyString());
    }

    @Test
    void ignoresDuplicateEvents() {
        TechnologyMetricRepository technologyRepository = mock(TechnologyMetricRepository.class);
        ProfileMetricRepository profileRepository = mock(ProfileMetricRepository.class);
        ProcessedAnalyticsEventRepository processedRepository = mock(ProcessedAnalyticsEventRepository.class);
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        AnalyticsService service = new AnalyticsService(technologyRepository, profileRepository,
                processedRepository, new ProfileClassifier(), kafkaTemplate,
                new ObjectMapper().findAndRegisterModules());
        when(processedRepository.existsByJobId(10L)).thenReturn(true);

        service.process(new MarketAnalyticsEvent(10L, "Java Developer", "Acme", null,
                java.util.List.of("Java"), null, null, null));

        verifyNoInteractions(technologyRepository, profileRepository, kafkaTemplate);
        verify(processedRepository, never()).save(any());
    }
}
