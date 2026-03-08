package com.jrecruiter.notificationservice.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification Service: Email sending for job recruitment events
 * 
 * - Job created notifications
 * - Application submitted confirmation
 * - Application status updates (interview, rejection, acceptance)
 * 
 * @author GitHub Copilot / TASK-018
 */
@Service
public class NotificationService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${notification.email.from}")
    private String fromEmail;
    
    @Value("${notification.email.from-name}")
    private String fromName;
    
    @Autowired
    public NotificationService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    /**
     * Send job created notification to candidates matching criteria
     * @param candidateEmail The recipient email
     * @param jobTitle Job title
     * @param companyName Company name
     * @param jobDescription Job description
     * @param jobId Job ID for deep linking
     */
    public void sendJobCreatedNotification(String candidateEmail, String jobTitle, 
                                          String companyName, String jobDescription, String jobId) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("jobTitle", jobTitle);
            templateVariables.put("companyName", companyName);
            templateVariables.put("jobDescription", jobDescription);
            templateVariables.put("jobLink", "https://app.jrecruiter.com/jobs/" + jobId);
            
            String htmlContent = resolveTemplate("job-created", templateVariables);
            sendHtmlEmail(candidateEmail, "New Job Opportunity: " + jobTitle, htmlContent);
        } catch (MessagingException e) {
            handleEmailError("Job Created", candidateEmail, e);
        }
    }
    
    /**
     * Send application submitted confirmation
     * @param candidateEmail Candidate email
     * @param candidateName Candidate name
     * @param jobTitle Job position applied for
     * @param applicationId Application ID for reference
     */
    public void sendApplicationSubmittedNotification(String candidateEmail, String candidateName,
                                                    String jobTitle, String applicationId) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("candidateName", candidateName);
            templateVariables.put("jobTitle", jobTitle);
            templateVariables.put("applicationId", applicationId);
            templateVariables.put("statusLink", "https://app.jrecruiter.com/applications/" + applicationId);
            
            String htmlContent = resolveTemplate("application-submitted", templateVariables);
            sendHtmlEmail(candidateEmail, "Application Submitted - " + jobTitle, htmlContent);
        } catch (MessagingException e) {
            handleEmailError("Application Submitted", candidateEmail, e);
        }
    }
    
    /**
     * Send interview invitation notification
     * @param candidateEmail Candidate email
     * @param candidateName Candidate name
     * @param jobTitle Job position
     * @param interviewDate Scheduled interview date/time
     * @param applicationId Application ID
     */
    public void sendInterviewInvitationNotification(String candidateEmail, String candidateName,
                                                   String jobTitle, String interviewDate, String applicationId) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("candidateName", candidateName);
            templateVariables.put("jobTitle", jobTitle);
            templateVariables.put("interviewDate", interviewDate);
            templateVariables.put("applicationId", applicationId);
            templateVariables.put("responseLink", "https://app.jrecruiter.com/applications/" + applicationId);
            
            String htmlContent = resolveTemplate("interview-invitation", templateVariables);
            sendHtmlEmail(candidateEmail, "Interview Invitation - " + jobTitle, htmlContent);
        } catch (MessagingException e) {
            handleEmailError("Interview Invitation", candidateEmail, e);
        }
    }
    
    /**
     * Send rejection notification
     * @param candidateEmail Candidate email
     * @param candidateName Candidate name
     * @param jobTitle Job position applied for
     * @param applicationId Application ID
     */
    public void sendRejectionNotification(String candidateEmail, String candidateName,
                                         String jobTitle, String applicationId) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("candidateName", candidateName);
            templateVariables.put("jobTitle", jobTitle);
            templateVariables.put("applicationId", applicationId);
            templateVariables.put("exploreLink", "https://app.jrecruiter.com/jobs");
            
            String htmlContent = resolveTemplate("application-rejected", templateVariables);
            sendHtmlEmail(candidateEmail, "Application Status Update", htmlContent);
        } catch (MessagingException e) {
            handleEmailError("Rejection", candidateEmail, e);
        }
    }
    
    /**
     * Send job offer notification
     * @param candidateEmail Candidate email
     * @param candidateName Candidate name
     * @param jobTitle Job position
     * @param offerExpiry Offer expiry date
     * @param applicationId Application ID
     */
    public void sendJobOfferNotification(String candidateEmail, String candidateName,
                                        String jobTitle, String offerExpiry, String applicationId) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("candidateName", candidateName);
            templateVariables.put("jobTitle", jobTitle);
            templateVariables.put("offerExpiry", offerExpiry);
            templateVariables.put("applicationId", applicationId);
            templateVariables.put("acceptLink", "https://app.jrecruiter.com/applications/" + applicationId + "/accept");
            
            String htmlContent = resolveTemplate("job-offer", templateVariables);
            sendHtmlEmail(candidateEmail, "Job Offer - " + jobTitle, htmlContent);
        } catch (MessagingException e) {
            handleEmailError("Job Offer", candidateEmail, e);
        }
    }
    
    /**
     * Send employer notification for new application
     * @param recruiterEmail Recruiter email
     * @param recruiterName Recruiter name
     * @param candidateName Candidate name
     * @param jobTitle Job position
     * @param applicationId Application ID
     */
    public void sendNewApplicationNotification(String recruiterEmail, String recruiterName,
                                              String candidateName, String jobTitle, String applicationId) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("recruiterName", recruiterName);
            templateVariables.put("candidateName", candidateName);
            templateVariables.put("jobTitle", jobTitle);
            templateVariables.put("applicationId", applicationId);
            templateVariables.put("reviewLink", "https://app.jrecruiter.com/admin/applications/" + applicationId);
            
            String htmlContent = resolveTemplate("new-application", templateVariables);
            sendHtmlEmail(recruiterEmail, "New Application - " + jobTitle, htmlContent);
        } catch (MessagingException e) {
            handleEmailError("New Application", recruiterEmail, e);
        }
    }
    
    /**
     * Send HTML email via SMTP
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML
        
        mailSender.send(mimeMessage);
    }
    
    /**
     * Resolve Thymeleaf template with variables
     */
    private String resolveTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process("email/" + templateName, context);
    }
    
    /**
     * Handle email sending errors (log + dead letter queue fallback)
     */
    private void handleEmailError(String eventType, String email, MessagingException e) {
        System.err.println("Error sending " + eventType + " email to " + email);
        e.printStackTrace();
        // TODO: Implement dead letter queue logging for failed emails
    }
}
