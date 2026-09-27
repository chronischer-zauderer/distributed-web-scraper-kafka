package com.scraperproducer.dataanalysis.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileClassifierTest {

    private final ProfileClassifier classifier = new ProfileClassifier();

    @Test
    void classifiesBackend() {
        assertThat(classifier.classify(List.of("Java", "Spring Boot"))).isEqualTo("BACKEND");
    }

    @Test
    void classifiesFrontend() {
        assertThat(classifier.classify(List.of("React", "TypeScript"))).isEqualTo("FRONTEND");
    }

    @Test
    void classifiesMixedOfferAsOther() {
        assertThat(classifier.classify(List.of("Java", "React"))).isEqualTo("OTHER");
    }

    @Test
    void classifiesMissingTechnologiesAsOther() {
        assertThat(classifier.classify(List.of())).isEqualTo("OTHER");
    }
}
