package com.jrecruiter.searchservice.infrastructure.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jrecruiter.searchservice.application.services.JobSearchService;
import com.jrecruiter.searchservice.application.services.AdvancedSearchService;
import com.jrecruiter.searchservice.domain.documents.JobSearchDocument;

import java.util.List;

/**
 * REST Controller: Job Search API
 * 
 * @author GitHub Copilot / TASK-017
 */
@RestController
@RequestMapping("/api/search/jobs")
public class SearchController {
    
    private final JobSearchService jobSearchService;
    private final AdvancedSearchService advancedSearchService;
    
    @Autowired
    public SearchController(JobSearchService jobSearchService, 
                          AdvancedSearchService advancedSearchService) {
        this.jobSearchService = jobSearchService;
        this.advancedSearchService = advancedSearchService;
    }
    
    /**
     * Simple keyword search
     * GET /api/search/jobs?q=java
     */
    @GetMapping
    public ResponseEntity<List<JobSearchDocument>> search(
            @RequestParam String q) {
        List<JobSearchDocument> results = jobSearchService.searchByKeyword(q);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Advanced search with filters
     * GET /api/search/jobs/advanced?q=spring&minSalary=50000&maxSalary=100000&remote=true
     */
    @GetMapping("/advanced")
    public ResponseEntity<List<JobSearchDocument>> advancedSearch(
            @RequestParam String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<JobSearchDocument> results = advancedSearchService.advancedSearch(
            q, location, minSalary, maxSalary, remote, industry, page, size);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Personalized search based on candidate profile
     * POST /api/search/jobs/personalized
     */
    @PostMapping("/personalized")
    public ResponseEntity<List<JobSearchDocument>> personalizedSearch(
            @RequestBody PersonalizedSearchRequest request) {
        
        List<JobSearchDocument> results = advancedSearchService.personalizedSearch(
            request.keyword,
            request.desiredCity,
            request.desiredCountry,
            request.experienceYears,
            request.skills,
            request.remotePreference,
            request.page,
            request.size);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * DTO for personalized search
     */
    public static class PersonalizedSearchRequest {
        public String keyword;
        public String desiredCity;
        public String desiredCountry;
        public Integer experienceYears;
        public String[] skills;
        public Boolean remotePreference;
        public int page = 0;
        public int size = 20;
    }
}
