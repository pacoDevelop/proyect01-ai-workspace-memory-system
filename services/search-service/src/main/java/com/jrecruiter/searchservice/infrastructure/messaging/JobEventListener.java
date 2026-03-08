package com.jrecruiter.searchservice.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.jrecruiter.searchservice.application.services.JobSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * RabbitMQ Listener for Job Events
 * Receives job events from Job-Service and indexes them.
 * 
 * @author GitHub Copilot / TASK-016
 */
@Component
public class JobEventListener {
    
    private final JobSearchService jobSearchService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public JobEventListener(JobSearchService jobSearchService, ObjectMapper objectMapper) {
        this.jobSearchService = jobSearchService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Listen for Job Events from the unified search queue
     */
    @RabbitListener(queues = RabbitConfig.SEARCH_QUEUE)
    public void handleJobEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            
            if ("JobPublished".equals(eventType)) {
                processJobPublished(event);
            } else if ("JobClosed".equals(eventType)) {
                processJobClosed(event);
            }
        } catch (Exception e) {
            System.err.println("Error processing job event: " + e.getMessage());
        }
    }
    
    private void processJobPublished(Map<String, Object> event) {
        try {
            String jobId = (String) event.get("jobId");
            String title = (String) event.get("title"); // Note: title might be a nested object if ValueObject serialization is tricky
            String description = (String) event.get("description");
            
            // Extract from nested if necessary (Title/Description are VOs in Job-Service)
            String titleStr = (title instanceof Map) ? (String)((Map)title).get("value") : title;
            String descStr = (description instanceof Map) ? (String)((Map)description).get("value") : description;

            Map<String, Object> location = (Map<String, Object>) event.get("location");
            String city = (location != null) ? (String) location.get("city") : "Unknown";
            
            Map<String, Object> salary = (Map<String, Object>) event.get("salary");
            Double minSalary = (salary != null) ? ((Number) salary.get("minAmount")).doubleValue() : 0.0;
            Double maxSalary = (salary != null) ? ((Number) salary.get("maxAmount")).doubleValue() : 0.0;
            String currency = (salary != null) ? (String) salary.get("currency") : "USD";
            
            jobSearchService.indexJob(jobId, titleStr, descStr, "Unknown", "PUBLISHED",
                minSalary, maxSalary, currency, true, city);
        } catch (Exception e) {
            System.err.println("Error indexing job: " + e.getMessage());
        }
    }
    
    private void processJobClosed(Map<String, Object> event) {
        try {
            String jobId = (String) event.get("jobId");
            jobSearchService.removeJobFromIndex(jobId);
        } catch (Exception e) {
            System.err.println("Error removing job: " + e.getMessage());
        }
    }
}
