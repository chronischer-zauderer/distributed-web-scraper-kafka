package com.scraperproducer.dataprocessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataExtractionService {

    /**
     * Procesa el HTML crudo purgado que proviene del bus de datos de Kafka.
     *
     * @param url La dirección web de origen (actúa como clave de auditoría).
     * @param htmlContent El cuerpo del HTML semántico ya limpio de scripts/estilos.
     */
    public void extractMarketData(String url, String htmlContent) {
        log.info("[PROCESADORCORE] Iniciando análisis sintáctico del HTML proveniente de: {}", url);

        // Explicación de Arquitectura:
        // Aquí es donde inyectaremos la persistencia a base de datos (PostgreSQL) y las
        // expresiones regulares (Regex) para aislar sueldos, cargos o precios.

        log.info("[PROCESADOR CORE] Datos analizados preliminarmente. Longitud del payload: {} caracteres.",
                htmlContent.length());
    }
}
