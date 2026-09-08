package com.scraperproducer.javaproducer.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.cache.mode", havingValue = "mock", matchIfMissing = true)
public class MockCacheService implements CacheService {
    @Override
    public Optional<String> get(String key) {
        log.info("[MOCK REDIS] Ejecutando operacion GET para: {} -> MISS", key);
        return Optional.empty();
    }

    @Override
    public void put(String key, String value) {
        log.info("[MOCK REDIS] Guardando datos purgados de forma simulada en cache.");
    }
}
