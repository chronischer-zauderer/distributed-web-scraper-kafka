package com.scraperproducer.javaproducer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic rawHtmlTopic() {
        // TopicBuilder nos permite definir las propiedades del canal de forma fluida.
        return TopicBuilder.name("raw-html")
                .partitions(3)    // Dividimos el canal en 3 carriles paralelos (Escalabilidad de nivel Senior)
                .replicas(1)      // Al estar en desarrollo local, mantenemos 1 sola copia del dato
                .build();
    }
}
