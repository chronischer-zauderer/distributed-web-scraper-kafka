package com.scraperproducer.dataprocessor.consumer;

import com.scraperproducer.dataprocessor.service.DataExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawHtmlConsumer {

    private final DataExtractionService dataExtractionService;

    // Vinculamos explícitamente la fábrica de contenedores configurada arriba
    @KafkaListener(
            topics = "raw-html",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeRawHtml(ConsumerRecord<String, String> record) {
        String htmlContent = record.value();
        String sourceUrl = record.key();
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new IllegalArgumentException("Mensaje raw-html vacío");
        }
        log.info("[KAFKA TRACER] ¡¡MENSAJE CAPTURADO FISICAMENTE EN LA CONSOLA LOCAL!!");
        log.info("[Payload Recibido] Longitud del HTML purgado: {} caracteres.", htmlContent.length());

        dataExtractionService.extractMarketData(sourceUrl, htmlContent);
    }
}
