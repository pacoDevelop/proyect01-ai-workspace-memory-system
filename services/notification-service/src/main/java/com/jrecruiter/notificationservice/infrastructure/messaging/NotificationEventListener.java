package com.jrecruiter.notificationservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.jrecruiter.notificationservice.application.services.NotificationService;

import java.util.Map;

/**
 * Notification Event Listener: RabbitMQ consumer for job and application events
 * 
 * Listens to:
 * - notification.job.created
 * - notification.application.submitted
 * - notification.application.interview
 * - notification.application.rejected
 * - notification.application.accepted
 * 
 * @author GitHub Copilot / TASK-018
 */
@Component
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public NotificationEventListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Listen for JobCreatedEvent
     * Triggers when a new job is published
     */
    @RabbitListener(queues = "notification.job.created")
    public void handleJobCreatedEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            
            String jobId = (String) payload.get("jobId");
            String jobTitle = (String) payload.get("title");
            String companyName = (String) payload.get("companyName");
            String description = (String) payload.get("description");
            String candidateEmails = (String) payload.get("candidateEmails");
            
            if (candidateEmails != null && !candidateEmails.isEmpty()) {
                for (String email : candidateEmails.split(",")) {
                    notificationService.sendJobCreatedNotification(
                        email.trim(), jobTitle, companyName, description, jobId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing JobCreatedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Listen for ApplicationSubmittedEvent
     * Triggers when a candidate submits an application
     */
    @RabbitListener(queues = "notification.application.submitted")
    public void handleApplicationSubmittedEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            
            String applicationId = (String) payload.get("applicationId");
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            
            notificationService.sendApplicationSubmittedNotification(
                candidateEmail, candidateName, jobTitle, applicationId);
            
            // Also notify recruiter
            String recruiterEmail = (String) payload.get("recruiterEmail");
            String recruiterName = (String) payload.get("recruiterName");
            
            if (recruiterEmail != null) {
                notificationService.sendNewApplicationNotification(
                    recruiterEmail, recruiterName, candidateName, jobTitle, applicationId);
            }
        } catch (Exception e) {
            System.err.println("Error processing ApplicationSubmittedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Listen for ApplicationInterviewEvent
     * Triggers when candidate is invited to interview
     */
    @RabbitListener(queues = "notification.application.interview")
    public void handleApplicationInterviewEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            
            String applicationId = (String) payload.get("applicationId");
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            String interviewDate = (String) payload.get("interviewDate");
            
            notificationService.sendInterviewInvitationNotification(
                candidateEmail, candidateName, jobTitle, interviewDate, applicationId);
        } catch (Exception e) {
            System.err.println("Error processing ApplicationInterviewEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Listen for ApplicationRejectedEvent
     * Triggers when application is rejected
     */
    @RabbitListener(queues = "notification.application.rejected")
    public void handleApplicationRejectedEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            
            String applicationId = (String) payload.get("applicationId");
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            
            notificationService.sendRejectionNotification(
                candidateEmail, candidateName, jobTitle, applicationId);
        } catch (Exception e) {
            System.err.println("Error processing ApplicationRejectedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Listen for ApplicationAcceptedEvent
     * Triggers when job offer is accepted
     */
    @RabbitListener(queues = "notification.application.accepted")
    public void handleApplicationAcceptedEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            
            String applicationId = (String) payload.get("applicationId");
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            String offerExpiry = (String) payload.get("offerExpiry");
            
            notificationService.sendJobOfferNotification(
                candidateEmail, candidateName, jobTitle, offerExpiry, applicationId);
        } catch (Exception e) {
            System.err.println("Error processing ApplicationAcceptedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
