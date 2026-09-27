package com.scraperproducer.dataprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraperproducer.dataprocessor.dto.JobOfferDto;
import com.scraperproducer.dataprocessor.dto.MarketAnalyticsEvent;
import com.scraperproducer.dataprocessor.model.JobOffer;
import com.scraperproducer.dataprocessor.parser.HtmlJobParser;
import com.scraperproducer.dataprocessor.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataExtractionService {

    private final JobOfferRepository jobOfferRepository;
    private final HtmlJobParser htmlJobParser;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void extractMarketData(String url, String htmlContent) {
        log.info("[PROCESSOR] Procesando oferta recibida desde {}", url);
        JobOfferDto dto = htmlJobParser.parse(htmlContent, url);
        // The same identity lets Kafka retries update one offer instead of inserting it again.
        String dedupeKey = buildDedupeKey(dto);
        JobOffer offer = jobOfferRepository.findFirstByDedupeKey(dedupeKey).orElseGet(JobOffer::new);

        offer.setTitle(dto.title());
        offer.setCompany(dto.company());
        offer.setSalary(dto.salary());
        offer.setRequiredSkills(dto.technologies().stream().collect(Collectors.joining(", ")));
        offer.setLocation(dto.location());
        offer.setSourceUrl(dto.sourceUrl());
        offer.setExtractedAt(dto.scrapedAt());
        offer.setDedupeKey(dedupeKey);

        JobOffer savedOffer = jobOfferRepository.save(offer);
        log.info("[POSTGRES] Oferta persistida con ID {}", savedOffer.getId());

        MarketAnalyticsEvent event = new MarketAnalyticsEvent(
                savedOffer.getId(), savedOffer.getTitle(), savedOffer.getCompany(), savedOffer.getSalary(),
                dto.technologies(), savedOffer.getLocation(), savedOffer.getSourceUrl(), savedOffer.getExtractedAt());
        publishEvent(event, savedOffer.getSourceUrl());
    }

    private String buildDedupeKey(JobOfferDto dto) {
        String identity = dto.sourceUrl() == null
                ? String.join("|", safe(dto.title()), safe(dto.company()), safe(dto.location()),
                String.valueOf(dto.salary()), String.join(",", dto.technologies()))
                : "URL|" + dto.sourceUrl();
        return sha256(identity);
    }

    private void publishEvent(MarketAnalyticsEvent event, String key) {
        final String jsonPayload;
        // Wait for Kafka confirmation so a failed publication rolls back this database transaction.
        try {
            jsonPayload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar market-analytics", exception);
        }

        try {
            var result = kafkaTemplate.send("market-analytics", key, jsonPayload).get();
            log.info("[KAFKA] market-analytics publicado en partición {}, offset {}",
                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación Kafka interrumpida", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Falló la publicación de market-analytics", exception);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte current : digest) {
                result.append(String.format("%02x", current));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }
}
