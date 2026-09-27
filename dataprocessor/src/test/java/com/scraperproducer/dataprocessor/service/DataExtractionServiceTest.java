package com.scraperproducer.dataprocessor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scraperproducer.dataprocessor.dto.JobOfferDto;
import com.scraperproducer.dataprocessor.model.JobOffer;
import com.scraperproducer.dataprocessor.parser.HtmlJobParser;
import com.scraperproducer.dataprocessor.repository.JobOfferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataExtractionServiceTest {

    @Test
    void persistsOfferAndPublishesAnalyticsEvent() {
        JobOfferRepository repository = mock(JobOfferRepository.class);
        HtmlJobParser parser = mock(HtmlJobParser.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        DataExtractionService service = new DataExtractionService(repository, parser, kafkaTemplate, objectMapper);

        LocalDateTime extractedAt = LocalDateTime.now();
        when(parser.parse("<h1>Java</h1>", "https://example.test/job/1"))
                .thenReturn(new JobOfferDto("Java Developer", "Acme", 5000.0,
                        List.of("Java", "Spring Boot"), "Cali", "https://example.test/job/1", extractedAt));
        when(repository.findFirstBySourceUrl("https://example.test/job/1")).thenReturn(Optional.empty());
        when(repository.findFirstByTitleAndCompanyAndLocation("Java Developer", "Acme", "Cali"))
                .thenReturn(Optional.empty());
        JobOffer saved = new JobOffer();
        saved.setId(7L);
        saved.setTitle("Java Developer");
        saved.setCompany("Acme");
        saved.setSalary(5000.0);
        saved.setRequiredSkills("Java, Spring Boot");
        saved.setLocation("Cali");
        saved.setSourceUrl("https://example.test/job/1");
        saved.setExtractedAt(extractedAt);
        when(repository.save(any(JobOffer.class))).thenReturn(saved);
        when(kafkaTemplate.send(eq("market-analytics"), eq("https://example.test/job/1"), any(String.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("test callback")));

        assertThrows(IllegalStateException.class,
                () -> service.extractMarketData("https://example.test/job/1", "<h1>Java</h1>"));

        verify(repository).save(any(JobOffer.class));
        verify(kafkaTemplate).send(eq("market-analytics"), eq("https://example.test/job/1"), any(String.class));
    }
}
