package com.scraperproducer.dataprocessor.parser;

import com.scraperproducer.dataprocessor.dto.JobOfferDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlJobParser {

    private static final Pattern SALARY_PATTERN = Pattern.compile("(?i)(?:\\$|COP\\s*)?\\s*([0-9][0-9.,]*)\\s*(k|mil|m|millones?)?");
    private static final List<String> TECHNOLOGY_NAMES = List.of(
            "Java", "Spring Boot", "Spring", "Python", "JavaScript", "TypeScript", "React",
            "Angular", "Node.js", "PostgreSQL", "MySQL", "SQL", "MongoDB", "Kafka", "Docker",
            "Kubernetes", "AWS", "Azure", "GCP", "Git", "Linux", "CI/CD"
    );

    public JobOfferDto parse(String html, String sourceUrl) {
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("HTML vacío");
        }

        Document document = Jsoup.parse(html);
        String title = firstText(document, "[itemprop=title]", ".job-title", "h1", "title");
        String company = firstText(document, "[itemprop=hiringOrganization]", ".company", ".company-name", "[class*=company]");
        String location = firstText(document, "[itemprop=jobLocation]", ".location", ".job-location", "[class*=location]");
        String visibleText = document.body() == null ? document.text() : document.body().text();
        // Restrict salary extraction to explicit fields; job pages often contain unrelated salary cards.
        Double salary = parseSalary(firstText(document, "[itemprop=baseSalary]", ".salary", ".job-salary"));
        List<String> technologies = extractTechnologies(document, visibleText);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("HTML sin título de oferta laboral");
        }

        return new JobOfferDto(
                normalize(title),
                normalize(company),
                salary,
                technologies,
                normalize(location),
                normalize(sourceUrl),
                LocalDateTime.now()
        );
    }

    private String firstText(Document document, String... selectors) {
        for (String selector : selectors) {
            Element element = document.select(selector).first();
            if (element != null && !element.text().isBlank()) {
                return element.text();
            }
        }
        return null;
    }

    private Double parseSalary(String salaryText) {
        if (salaryText == null) {
            return null;
        }
        Matcher matcher = SALARY_PATTERN.matcher(salaryText);
        List<Double> values = new ArrayList<>();
        while (matcher.find() && values.size() < 2) {
            try {
                double value = Double.parseDouble(matcher.group(1).replace(".", "").replace(",", ""));
                String multiplier = matcher.group(2);
                if (multiplier != null) {
                    String normalizedMultiplier = multiplier.toLowerCase(Locale.ROOT);
                    value *= normalizedMultiplier.equals("k") || normalizedMultiplier.equals("mil")
                            ? 1_000 : 1_000_000;
                }
                values.add(value);
            } catch (NumberFormatException ignored) {
                // Ignore malformed salary fragments and keep looking for a valid value.
            }
        }
        if (values.isEmpty()) {
            return null;
        }
        return values.size() == 2 ? (values.get(0) + values.get(1)) / 2 : values.get(0);
    }

    private List<String> extractTechnologies(Document document, String visibleText) {
        Map<String, String> normalized = new LinkedHashMap<>();
        String skillsText = firstText(document, "[itemprop=skills]", ".skills", ".technologies", ".requirements", "[class*=skill]");
        String haystack = ((skillsText == null ? "" : skillsText) + " " + visibleText).toLowerCase(Locale.ROOT);

        for (String technology : TECHNOLOGY_NAMES) {
            if (technology.equals("Spring") && normalized.containsKey("spring boot")) {
                continue;
            }
            String escapedTechnology = Pattern.quote(technology.toLowerCase(Locale.ROOT));
            if (Pattern.compile("(?<![a-z0-9])" + escapedTechnology + "(?![a-z0-9])")
                    .matcher(haystack).find()) {
                normalized.put(technology.toLowerCase(Locale.ROOT), technology);
            }
        }
        return new ArrayList<>(normalized.values());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.isBlank() ? null : normalized;
    }
}
