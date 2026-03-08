package com.jrecruiter.searchservice.infrastructure.adapters;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.jrecruiter.searchservice.domain.documents.JobSearchDocument;

import java.util.List;

/**
 * Elasticsearch Repository for Job Search
 * 
 * @author GitHub Copilot / TASK-016
 */
@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobSearchDocument, String> {
    
    // Full-text search on title and description
    List<JobSearchDocument> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String title, String description);
    
    // Filter by status
    List<JobSearchDocument> findByStatus(String status);
    
    // Filter by company
    List<JobSearchDocument> findByCompanyName(String companyName);
    
    // Filter by remote
    List<JobSearchDocument> findByRemote(Boolean remote);
    
    // Filter by salary range
    List<JobSearchDocument> findByMinSalaryGreaterThanEqualAndMaxSalaryLessThanEqual(
        Double minSalary, Double maxSalary);
    
    // Combined search: title + status + remote
    List<JobSearchDocument> findByTitleContainingIgnoreCaseAndStatusAndRemote(
        String title, String status, Boolean remote);
}
