package com.scraperproducer.dataprocessor.consumer;

import com.scraperproducer.dataprocessor.service.DataExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawHtmlConsumer {

    private final DataExtractionService dataExtractionService;

    // Vinculamos explícitamente la fábrica de contenedores configurada arriba
    @KafkaListener(
            topics = "raw-html",
            groupId = "market-processor-group-vFinal", // mismo grupo que la factory para no dividir el consumo
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeRawHtml(String htmlContent) {
        log.info("[KAFKA TRACER] ¡¡MENSAJE CAPTURADO FISICAMENTE EN LA CONSOLA LOCAL!!");
        log.info("[Payload Recibido] Longitud del HTML purgado: {} caracteres.", htmlContent.length());

        dataExtractionService.extractMarketData("Origen detectado desde Kafka", htmlContent);
    }
}
