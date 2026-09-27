package com.scraperproducer.javaproducer.controller;

import com.scraperproducer.javaproducer.dto.ScrapeRequest;
import com.scraperproducer.javaproducer.service.ScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scrape")
@RequiredArgsConstructor
public class ScraperController {

    private final ScraperService scraperService;

    @PostMapping
    public ResponseEntity<String> executeScraping(@RequestBody ScrapeRequest request) {
        String result = scraperService.processUrl(request.getUrl());
        return ResponseEntity.ok(result);
    }
}
