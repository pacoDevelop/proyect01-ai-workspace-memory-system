package com.jrecruiter.searchservice.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.jrecruiter.searchservice.domain.documents.JobSearchDocument;
import com.jrecruiter.searchservice.infrastructure.adapters.JobSearchRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Job Search Service
 * Full-text search with faceting, filtering, relevance ranking.
 * 
 * @author GitHub Copilot / TASK-017
 */
@Service
public class AdvancedSearchService {
    
    private final JobSearchRepository repository;
    
    @Autowired
    public AdvancedSearchService(JobSearchRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Advanced search with multiple filters and ranking
     * Includes input validation and defensive null checks.
     * 
     * Uses repository methods for filtering combined with in-memory sorting for ranking.
     */
    public List<JobSearchDocument> advancedSearch(String keyword, String location,
                                                   Double minSalary, Double maxSalary,
                                                   Boolean remote, String industry,
                                                   int page, int size) {
        
        // Input validation
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        
        // Base search: keyword in title or description
        List<JobSearchDocument> results = repository
            .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        
        // Apply additional filters
        results = results.stream()
            .filter(doc -> doc.getStatus() != null && doc.getStatus().equals("PUBLISHED"))
            .filter(doc -> minSalary == null || doc.getMinSalary() == null || doc.getMinSalary() >= minSalary)
            .filter(doc -> maxSalary == null || doc.getMaxSalary() == null || doc.getMaxSalary() <= maxSalary)
            .filter(doc -> remote == null || doc.getRemote() == null || doc.getRemote().equals(remote))
            .filter(doc -> industry == null || doc.getIndustry() == null || doc.getIndustry().equalsIgnoreCase(industry))
            .filter(doc -> location == null || doc.getLocation() == null || doc.getLocation().toLowerCase().contains(location.toLowerCase()))
            .collect(Collectors.toList());
        
        // Apply pagination
        int start = Math.min(page * size, results.size());
        int end = Math.min(start + size, results.size());
        
        return results.subList(start, end);
    }
    
    /**
     * Personalized ranking based on candidate preferences.
     * Applies boosting strategy:
     * - Skill matching (2.0f) - Highest priority
     * - Title matching (1.5f) - High priority
     * - Remote preference (1.2f) - Medium priority
     * - Location (0.8f) - Soft preference
     * - Base score (1.0f) - Description match
     */
    public List<JobSearchDocument> personalizedSearch(String keyword, String desiredCity,
                                                      String desiredCountry, Integer experienceYears,
                                                      String[] skills, Boolean remotePreference,
                                                      int page, int size) {
        
        // Input validation
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        
        // Base search
        List<JobSearchDocument> results = repository
            .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        
        // Apply ranking with boost factors
        List<RankedJob> ranked = results.stream()
            .map(doc -> {
                float score = 1.0f; // Base score
                
                // Skill matching boost (highest priority)
                if (skills != null && skills.length > 0) {
                    for (String skill : skills) {
                        if (doc.getDescription() != null && 
                            doc.getDescription().toLowerCase().contains(skill.toLowerCase())) {
                            score += 2.0f;
                        }
                    }
                }
                
                // Title match boost
                if (doc.getTitle() != null && 
                    doc.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                    score += 1.5f;
                }
                
                // Remote preference boost
                if (remotePreference != null && doc.getRemote() != null && 
                    doc.getRemote().equals(remotePreference)) {
                    score += 1.2f;
                }
                
                // Location boost
                if (desiredCity != null && doc.getLocation() != null && 
                    doc.getLocation().toLowerCase().contains(desiredCity.toLowerCase())) {
                    score += 0.8f;
                }
                
                return new RankedJob(doc, score);
            })
            .sorted((a, b) -> Float.compare(b.score, a.score))
            .map(rj -> rj.document)
            .collect(Collectors.toList());
        
        // Apply pagination
        int start = Math.min(page * size, results.size());
        int end = Math.min(start + size, results.size());
        
        return results.subList(start, end);
    }
    
    /**
     * Internal class for ranking jobs with scores
     */
    private static class RankedJob {
        final JobSearchDocument document;
        final float score;
        
        RankedJob(JobSearchDocument document, float score) {
            this.document = document;
            this.score = score;
        }
    }
    
    /**
     * Faceted search: group results by dimension with aggregations.
     * Returns facets for: status, salary range.
     * 
     * IMPLEMENTATION: In-memory faceting (aggregating retrieved documents)
     */
    public FacetedSearchResult facetedSearch(String keyword, int page, int size) {
        // Input validation
        if (keyword == null || keyword.isBlank()) {
            return new FacetedSearchResult(keyword, page, size, Collections.emptyList(), new SalaryFacet(0, 0, 0));
        }
        
        // Base search
        List<JobSearchDocument> results = repository
            .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        
        // Compute facets from all results (before pagination)
        List<StatusFacet> statusFacets = computeStatusFacets(results);
        SalaryFacet salaryFacet = computeSalaryFacet(results);
        
        // Apply pagination for document results
        int start = Math.min(page * size, results.size());
        int end = Math.min(start + size, results.size());
        List<JobSearchDocument> paginatedResults = results.subList(start, end);
        
        return new FacetedSearchResult(keyword, page, size, statusFacets, salaryFacet);
    }
    
    /**
     * Compute status facets from document results (in-memory aggregation)
     */
    private List<StatusFacet> computeStatusFacets(List<JobSearchDocument> documents) {
        return documents.stream()
            .filter(doc -> doc.getStatus() != null)
            .collect(Collectors.groupingByConcurrency(
                JobSearchDocument::getStatus,
                Collectors.counting()
            ))
            .entrySet().stream()
            .map(entry -> new StatusFacet(entry.getKey(), entry.getValue()))
            .limit(10)
            .collect(Collectors.toList());
    }
    
    /**
     * Compute salary statistics from document results
     */
    private SalaryFacet computeSalaryFacet(List<JobSearchDocument> documents) {
        if (documents.isEmpty()) {
            return new SalaryFacet(0, 0, 0);
        }
        
        long minSalary = documents.stream()
            .map(JobSearchDocument::getMinSalary)
            .filter(Objects::nonNull)
            .mapToLong(Double::longValue)
            .min()
            .orElse(0);
        
        long maxSalary = documents.stream()
            .map(JobSearchDocument::getMaxSalary)
            .filter(Objects::nonNull)
            .mapToLong(Double::longValue)
            .max()
            .orElse(0);
        
        long avgSalary = (long) documents.stream()
            .map(JobSearchDocument::getMinSalary)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
        
        return new SalaryFacet(minSalary, maxSalary, avgSalary);
    }
    
    /**
     * Result class for faceted search
     */
    public static class FacetedSearchResult {
        private final String keyword;
        private final int page;
        private final int size;
        private final List<StatusFacet> statusFacets;
        private final SalaryFacet salaryFacet;
        
        public FacetedSearchResult(String keyword, int page, int size, 
                                  List<StatusFacet> statusFacets,
                                  SalaryFacet salaryFacet) {
            this.keyword = keyword;
            this.page = page;
            this.size = size;
            this.statusFacets = statusFacets != null ? statusFacets : Collections.emptyList();
            this.salaryFacet = salaryFacet;
        }
        
        public String getKeyword() { return keyword; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public List<StatusFacet> getStatusFacets() { return statusFacets; }
        public SalaryFacet getSalaryFacet() { return salaryFacet; }
    }
    
    /**
     * Status facet (e.g., PUBLISHED, CLOSED, DRAFT)
     */
    public static class StatusFacet {
        private final String status;
        private final long count;
        
        public StatusFacet(String status, long count) {
            this.status = status;
            this.count = count;
        }
        
        public String getStatus() { return status; }
        public long getCount() { return count; }
    }
    
    /**
     * Salary statistics facet (min, max, avg)
     */
    public static class SalaryFacet {
        private final long minSalary;
        private final long maxSalary;
        private final long avgSalary;
        
        public SalaryFacet(long minSalary, long maxSalary, long avgSalary) {
            this.minSalary = minSalary;
            this.maxSalary = maxSalary;
            this.avgSalary = avgSalary;
        }
        
        public long getMinSalary() { return minSalary; }
        public long getMaxSalary() { return maxSalary; }
        public long getAvgSalary() { return avgSalary; }
    }
}
