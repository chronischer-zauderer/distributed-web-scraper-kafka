package com.scraperproducer.analyticsdashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraperproducer.analyticsdashboard.dto.AnalyticsDashboardEvent;
import com.scraperproducer.analyticsdashboard.entity.DashboardSnapshot;
import com.scraperproducer.analyticsdashboard.repository.DashboardSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardSnapshotService {

    private static final long LATEST_SNAPSHOT_ID = 1L;

    private final ObjectMapper objectMapper;
    private final DashboardSnapshotRepository repository;
    private final AtomicReference<AnalyticsDashboardEvent> latest = new AtomicReference<>();

    @KafkaListener(topics = "analytics-dashboard", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consume(String payload) {
        try {
            AnalyticsDashboardEvent event = objectMapper.readValue(payload, AnalyticsDashboardEvent.class);
            if (event.timestamp() == null || event.periods() == null) {
                throw new IllegalArgumentException("Snapshot sin timestamp o periodos");
            }

            DashboardSnapshot snapshot = repository.findById(LATEST_SNAPSHOT_ID).orElseGet(DashboardSnapshot::new);
            snapshot.setId(LATEST_SNAPSHOT_ID);
            snapshot.setEventTimestamp(event.timestamp());
            snapshot.setPayload(payload);
            snapshot.setReceivedAt(LocalDateTime.now());
            repository.save(snapshot);
            latest.set(event);
            log.info("[DASHBOARD] Snapshot recibido y almacenado: {} periodos", event.periods().size());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("JSON analytics-dashboard inválido", exception);
        }
    }

    @Transactional(readOnly = true)
    public Optional<AnalyticsDashboardEvent> latestSnapshot() {
        AnalyticsDashboardEvent inMemory = latest.get();
        if (inMemory != null) {
            return Optional.of(inMemory);
        }
        return repository.findById(LATEST_SNAPSHOT_ID).flatMap(snapshot -> {
            try {
                return Optional.of(objectMapper.readValue(snapshot.getPayload(), AnalyticsDashboardEvent.class));
            } catch (JsonProcessingException exception) {
                log.error("[DASHBOARD] No se pudo leer el último snapshot persistido", exception);
                return Optional.empty();
            }
        });
    }
}
