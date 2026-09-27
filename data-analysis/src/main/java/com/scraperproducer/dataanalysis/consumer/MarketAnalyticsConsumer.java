package com.scraperproducer.dataanalysis.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraperproducer.dataanalysis.dto.MarketAnalyticsEvent;
import com.scraperproducer.dataanalysis.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketAnalyticsConsumer {

    private final ObjectMapper objectMapper;
    private final AnalyticsService analyticsService;

        @KafkaListener(topics = "market-analytics", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(String payload) {
        try {
            MarketAnalyticsEvent event = objectMapper.readValue(payload, MarketAnalyticsEvent.class);
            analyticsService.process(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("JSON market-analytics inválido", exception);
        }
    }
}
