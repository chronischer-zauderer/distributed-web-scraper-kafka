package com.scraperproducer.javaproducer.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.mode", havingValue = "kafka")
public class KafkaHtmlPublisher implements HtmlPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publishRawHtml(String url, String htmlContent) {
        log.info("[KAFKA REAL] Preparando envío de evento al tópico 'raw-html'...");

        try {

            // The URL key preserves source identity for the downstream deduplication step.
            kafkaTemplate.send("raw-html", url, htmlContent)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("[KAFKA SUCCESS] Mensaje acuse de recibo confirmado. Partición: {}, Offset: {}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("[KAFKA ERROR] El clúster rechazó el mensaje o no respondió. Motivo: {}", ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("[KAFKA CRITICAL] Fallo catastrófico al intentar comunicar con el cliente Kafka: {}", e.getMessage());
        }
    }
}
