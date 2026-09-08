package com.scraperproducer.javaproducer.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cache.mode", havingValue = "redis")
public class RedisCacheService implements CacheService {

    private final StringRedisTemplate redisTemplate;

    private static final long CACHE_TTL_HOURS = 1;

    @Override
    public Optional<String> get(String key) {

        try {
            log.info("[REDIS] Buscando en caché la URL: {}", key);

            // opsForValue() le indica a Spring que vamos a ejecutar comandos para strings simples (Comando GET de Redis).
            String cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue != null) {
                log.info("[REDIS HIT] ¡Encontrado! Retornando datos desde la memoria RAM instantáneamente.");
                return Optional.of(cachedValue);
            }
        } catch (Exception e) {
            log.error("[REDIS ERROR] Falló la conexión con Redis. El sistema continuará sin caché. Motivo: {}", e.getMessage());
        }

        log.info("[REDIS MISS] La URL no existe en la caché. Se requiere petición HTTP externa.");
        return Optional.empty();
    }

    @Override
    public void put(String key, String value) {
        try {
            log.info("[REDIS] Almacenando HTML purgado en caché (TTL: {} hora)", CACHE_TTL_HOURS);

            redisTemplate.opsForValue().set(key, value, CACHE_TTL_HOURS, TimeUnit.HOURS);

        } catch (Exception e) {
            log.error("[REDIS ERROR] No se pudo escribir el dato en la caché: {}", e.getMessage());
        }
    }
}
