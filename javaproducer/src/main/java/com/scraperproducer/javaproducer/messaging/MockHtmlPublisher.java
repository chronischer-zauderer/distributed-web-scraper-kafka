package com.scraperproducer.javaproducer.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.messaging.mode", havingValue = "mock", matchIfMissing = true)
public class MockHtmlPublisher implements HtmlPublisher {
    @Override
    public void publishRawHtml(String url, String htmlContent) {
        log.info("[MOCK KAFKA] Publicando asincronamente en el topic 'raw-html' para el origen: {}", url);
    }
}
