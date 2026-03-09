package com.jrecruiter.notificationservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.jrecruiter.notificationservice.application.services.NotificationService;

import java.util.Map;

/**
 * Notification Event Listener: RabbitMQ consumer for job and application events
 * 
 * Listens to: job-notification-queue (fanout from job-events exchange)
 * Routes events internally based on event_type:
 * - JobCreated: Send notifications to matching candidates
 * - ApplicationSubmitted: Confirmation to candidate + alert to recruiter
 * - InterviewScheduled: Interview invitation
 * - ApplicationRejected: Rejection notification
 * - ApplicationAccepted: Acceptance notification
 * 
 * @author GitHub Copilot / TASK-018
 */
@Slf4j
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
     * Main listener for all job/application events
     * Consumes from job-notification-queue (fanout binding to job-events exchange)
     */
    @RabbitListener(queues = "job-notification-queue")
    public void handleJobEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String eventType = (String) payload.get("event_type");
            
            log.info("Received event from RabbitMQ: event_type={}", eventType);
            
            switch (eventType != null ? eventType : "") {
                case "JobCreated":
                    handleJobCreatedEvent(payload);
                    break;
                case "ApplicationSubmitted":
                    handleApplicationSubmittedEvent(payload);
                    break;
                case "InterviewScheduled":
                    handleInterviewScheduledEvent(payload);
                    break;
                case "ApplicationRejected":
                    handleApplicationRejectedEvent(payload);
                    break;
                case "ApplicationAccepted":
                    handleApplicationAcceptedEvent(payload);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing event message", e);
            throw new RuntimeException("Failed to process event", e);
        }
    }
    
    /**
     * Handle JobCreated event
     * Sends notification to candidates matching criteria
     */
    private void handleJobCreatedEvent(Map<String, Object> payload) {
        try {
            String jobId = (String) payload.get("jobId");
            String jobTitle = (String) payload.get("title");
            String companyName = (String) payload.get("companyName");
            String description = (String) payload.get("description");
            String candidateEmails = (String) payload.get("candidateEmails");
            
            if (candidateEmails != null && !candidateEmails.isEmpty()) {
                String[] emails = candidateEmails.split(",");
                for (String email : emails) {
                    notificationService.sendJobCreatedNotification(
                        email.trim(),
                        jobTitle,
                        companyName,
                        description,
                        jobId
                    );
                }
                log.info("JobCreated notification sent to {} candidates for job: {}", emails.length, jobTitle);
            }
        } catch (Exception e) {
            log.error("Error handling JobCreated event", e);
            throw new RuntimeException("Failed to handle JobCreated event", e);
        }
    }
    
    /**
     * Handle ApplicationSubmitted event
     * Sends confirmation to candidate + alert to recruiter
     */
    private void handleApplicationSubmittedEvent(Map<String, Object> payload) {
        try {
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            String applicationId = (String) payload.get("applicationId");
            
            // Send confirmation to candidate
            notificationService.sendApplicationSubmittedNotification(
                candidateEmail,
                candidateName,
                jobTitle,
                applicationId
            );
            
            log.info("ApplicationSubmitted confirmation sent to candidate: {}", candidateEmail);
        } catch (Exception e) {
            log.error("Error handling ApplicationSubmitted event", e);
            throw new RuntimeException("Failed to handle ApplicationSubmitted event", e);
        }
    }
    
    /**
     * Handle InterviewScheduled event
     * Sends interview invitation to candidate
     */
    private void handleInterviewScheduledEvent(Map<String, Object> payload) {
        try {
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            String interviewDate = (String) payload.get("interviewDate");
            String applicationId = (String) payload.get("applicationId");
            
            notificationService.sendInterviewInvitationNotification(
                candidateEmail,
                candidateName,
                jobTitle,
                interviewDate,
                applicationId
            );
            
            log.info("Interview invitation sent to candidate: {}", candidateEmail);
        } catch (Exception e) {
            log.error("Error handling InterviewScheduled event", e);
            throw new RuntimeException("Failed to handle InterviewScheduled event", e);
        }
    }
    
    /**
     * Handle ApplicationRejected event
     */
    private void handleApplicationRejectedEvent(Map<String, Object> payload) {
        try {
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            String applicationId = (String) payload.get("applicationId");
            
            notificationService.sendRejectionNotification(
                candidateEmail,
                candidateName,
                jobTitle,
                applicationId
            );
            
            log.info("Rejection notification sent to candidate: {}", candidateEmail);
        } catch (Exception e) {
            log.error("Error handling ApplicationRejected event", e);
            throw new RuntimeException("Failed to handle ApplicationRejected event", e);
        }
    }
    
    /**
     * Handle ApplicationAccepted event (Job Offer)
     */
    private void handleApplicationAcceptedEvent(Map<String, Object> payload) {
        try {
            String candidateEmail = (String) payload.get("candidateEmail");
            String candidateName = (String) payload.get("candidateName");
            String jobTitle = (String) payload.get("jobTitle");
            String offerExpiry = (String) payload.get("offerExpiry");
            String applicationId = (String) payload.get("applicationId");
            
            notificationService.sendJobOfferNotification(
                candidateEmail,
                candidateName,
                jobTitle,
                offerExpiry,
                applicationId
            );
            
            log.info("Job offer notification sent to candidate: {}", candidateEmail);
        } catch (Exception e) {
            log.error("Error handling ApplicationAccepted event", e);
            throw new RuntimeException("Failed to handle ApplicationAccepted event", e);
        }
    }
}
