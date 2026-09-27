package com.scraperproducer.dataanalysis.service;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class ProfileClassifier {

    private static final Set<String> BACKEND = Set.of(
            "java", "spring", "spring boot", "node.js", "nestjs", "python", "django",
            "fastapi", "c#", ".net", "go", "kafka", "sql", "postgresql", "mysql", "mongodb"
    );
    private static final Set<String> FRONTEND = Set.of(
            "react", "angular", "vue", "next.js", "svelte", "javascript", "typescript"
    );

    public String classify(Iterable<String> technologies) {
        boolean backend = false;
        boolean frontend = false;
        for (String technology : technologies) {
            if (technology == null) {
                continue;
            }
            String normalized = technology.trim().toLowerCase(Locale.ROOT);
            backend |= BACKEND.contains(normalized);
            frontend |= FRONTEND.contains(normalized);
        }
        if (backend && !frontend) {
            return "BACKEND";
        }
        if (frontend && !backend) {
            return "FRONTEND";
        }
        // Mixed or technology-free offers stay OTHER instead of being counted twice.
        return "OTHER";
    }
}
