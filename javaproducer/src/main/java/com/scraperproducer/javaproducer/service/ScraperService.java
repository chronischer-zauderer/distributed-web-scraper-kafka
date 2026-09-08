package com.scraperproducer.javaproducer.service;

import com.scraperproducer.javaproducer.cache.CacheService;
import com.scraperproducer.javaproducer.messaging.HtmlPublisher;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private final CacheService cacheService;
    private final HtmlPublisher htmlPublisher;
    private final RestClient restClient = RestClient.create();

    public String processUrl(String url) {
        // 1. Capa de Caché (Redis Check)
        Optional<String> cachedHtml = cacheService.get(url);
        if (cachedHtml.isPresent()) {
            log.info("[CACHE HIT] Retornando HTML directamente desde la Cache");
            return cachedHtml.get();
        }

        // 2. Descarga y Limpieza (Peticion HTTP Protegida)
        String cleanedHtml = fetchWithCircuitBreaker(url);

        // 3. Persistencia rapida en Cache
        cacheService.put(url, cleanedHtml);

        // 4. Publicacion asincrona del evento
        htmlPublisher.publishRawHtml(url, cleanedHtml);

        return cleanedHtml;
    }

    @CircuitBreaker(name = "scraperService", fallbackMethod = "fallbackFetch")
    private String fetchWithCircuitBreaker(String url) {
        log.info("[HTTP] Realizando peticion de extraccion a la URL: {}", url);

        String rawHtml = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        if (rawHtml == null || rawHtml.isEmpty()) {
            throw new RuntimeException("El servidor destino retorno un cuerpo vacio");
        }

        // Extractor Crudo empleando Jsoup para purgar el arbol DOM
        Document doc = Jsoup.parse(rawHtml);
        doc.select("script, style, iframe, noscript").remove();

        log.info("[Jsoup] HTML purgado con éxito.");
        return doc.body().html();
    }

    // Metodo de contención si el Circuit Breaker detecta fallos masivos en la red externa
    private String fallbackFetch(String url, Throwable t) {
        log.error("[CIRCUIT BREAKER ABIERTO] Falló la petición HTTP. Lanzando respuesta de gracia. Motivo: {}", t.getMessage());
        return "<html><body><p>Contenido temporalmente no disponible por políticas de resiliencia del microservicio.</p></body></html>";
    }
}
