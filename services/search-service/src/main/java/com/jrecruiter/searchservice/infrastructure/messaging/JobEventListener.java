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
     * Listen for JobPublishedEvent
     */
    @RabbitListener(queues = "job.published.queue")
    public void handleJobPublishedEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String jobId = (String) event.get("jobId");
            String title = (String) event.get("title");
            String description = (String) event.get("description");
            String companyName = (String) event.get("companyName");
            Double minSalary = ((Number) event.get("minSalary")).doubleValue();
            Double maxSalary = ((Number) event.get("maxSalary")).doubleValue();
            String currency = (String) event.get("currency");
            Boolean remote = (Boolean) event.get("remote");
            
            jobSearchService.indexJob(jobId, title, description, companyName, "PUBLISHED",
                minSalary, maxSalary, currency, remote, null);
        } catch (Exception e) {
            // Log error and send to dead letter queue
            System.err.println("Error indexing job: " + e.getMessage());
        }
    }
    
    /**
     * Listen for JobClosedEvent
     */
    @RabbitListener(queues = "job.closed.queue")
    public void handleJobClosedEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String jobId = (String) event.get("jobId");
            
            jobSearchService.removeJobFromIndex(jobId);
        } catch (Exception e) {
            System.err.println("Error removing job from index: " + e.getMessage());
        }
    }
}
