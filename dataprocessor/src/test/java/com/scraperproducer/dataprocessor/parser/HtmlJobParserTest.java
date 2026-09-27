package com.scraperproducer.dataprocessor.parser;

import com.scraperproducer.dataprocessor.dto.JobOfferDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HtmlJobParserTest {

    private final HtmlJobParser parser = new HtmlJobParser();

    @Test
    void extractsAndNormalizesOfferFields() {
        String html = """
                <html><body>
                  <h1> Backend   Developer </h1>
                  <div class="company"> Example Corp </div>
                  <div class="location"> Cali, Colombia </div>
                  <div class="salary">$ 5.000.000</div>
                  <div class="skills">Java, java, Spring Boot, PostgreSQL, Docker</div>
                </body></html>
                """;

        JobOfferDto offer = parser.parse(html, "https://example.test/jobs/1");

        assertThat(offer.title()).isEqualTo("Backend Developer");
        assertThat(offer.company()).isEqualTo("Example Corp");
        assertThat(offer.location()).isEqualTo("Cali, Colombia");
        assertThat(offer.salary()).isEqualTo(5000000.0);
        assertThat(offer.technologies()).containsExactly("Java", "Spring Boot", "PostgreSQL", "Docker");
    }

    @Test
    void acceptsOfferWithoutSalary() {
        JobOfferDto offer = parser.parse("<h1>Java Developer</h1><div class='company'>Acme</div>", null);

        assertThat(offer.salary()).isNull();
        assertThat(offer.title()).isEqualTo("Java Developer");
    }

    @Test
    void usesMidpointForSalaryRange() {
        JobOfferDto offer = parser.parse("<h1>Backend Developer</h1><div class='salary'>$4M - $6M</div>", null);

        assertThat(offer.salary()).isEqualTo(5000000.0);
    }

    @Test
    void rejectsHtmlWithoutTitle() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("<p>Nothing useful</p>", null));
    }
}
