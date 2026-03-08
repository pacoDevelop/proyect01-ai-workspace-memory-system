package com.jrecruiter.searchservice.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jrecruiter.searchservice.domain.documents.JobSearchDocument;
import com.jrecruiter.searchservice.infrastructure.adapters.JobSearchRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Job Search Service
 * Handles indexing and searching jobs in Elasticsearch.
 * 
 * @author GitHub Copilot / TASK-016
 */
@Service
public class JobSearchService {
    
    private final JobSearchRepository repository;
    
    @Autowired
    public JobSearchService(JobSearchRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Index a job in Elasticsearch
     */
    public void indexJob(String jobId, String title, String description, String companyName,
                        String status, Double minSalary, Double maxSalary, String currency,
                        Boolean remote, LocalDateTime publishedAt) {
        
        JobSearchDocument doc = new JobSearchDocument();
        doc.setJobId(jobId);
        doc.setTitle(title);
        doc.setDescription(description);
        doc.setCompanyName(companyName);
        doc.setStatus(status);
        doc.setMinSalary(minSalary);
        doc.setMaxSalary(maxSalary);
        doc.setCurrency(currency);
        doc.setRemote(remote);
        doc.setPublishedAt(publishedAt);
        doc.setIndexedAt(LocalDateTime.now());
        
        repository.save(doc);
    }
    
    /**
     * Search jobs by keyword
     */
    public List<JobSearchDocument> searchByKeyword(String keyword) {
        return repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            keyword, keyword);
    }
    
    /**
     * Search active jobs only
     */
    public List<JobSearchDocument> searchActiveJobs(String keyword) {
        return repository.findByTitleContainingIgnoreCaseAndStatusAndRemote(
            keyword, "PUBLISHED", null);
    }
    
    /**
     * Filter by remote
     */
    public List<JobSearchDocument> findRemoteJobs(Boolean remote) {
        return repository.findByRemote(remote);
    }
    
    /**
     * Filter by salary range
     */
    public List<JobSearchDocument> findByMinSalaryRange(Double minAmount, Double maxAmount) {
        return repository.findByMinSalaryGreaterThanEqualAndMaxSalaryLessThanEqual(
            minAmount, maxAmount);
    }
    
    /**
     * Remove job from index
     */
    public void removeJobFromIndex(String jobId) {
        repository.deleteById(jobId);
    }
}
