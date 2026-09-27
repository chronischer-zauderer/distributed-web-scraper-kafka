package com.scraperproducer.dataanalysis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarketAnalyticsEventTest {

    @Test
    void readsLegacyEventNamesFromKafka() throws Exception {
        MarketAnalyticsEvent event = new ObjectMapper().readValue(
                "{\"id\":42,\"puesto\":\"Java Developer\",\"empresa\":\"Acme\",\"salario\":5000000}",
                MarketAnalyticsEvent.class);

        assertThat(event.jobId()).isEqualTo(42L);
        assertThat(event.title()).isEqualTo("Java Developer");
        assertThat(event.salary()).isEqualTo(5000000.0);
    }
}
