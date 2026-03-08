package com.jrecruiter.jobservice.domain.events;

import java.time.Instant;
import java.util.UUID;

import com.jrecruiter.jobservice.domain.valueobjects.JobTitle;
import com.jrecruiter.jobservice.domain.valueobjects.JobDescription;
import com.jrecruiter.jobservice.domain.valueobjects.JobLocation;
import com.jrecruiter.jobservice.domain.valueobjects.JobSalary;

/**
 * Domain Event: Job Published
 * 
 * Emitted when a job is transitioned from DRAFT to PUBLISHED status.
 * This event triggers downstream services (Search, Notification, etc).
 * 
 * @author GitHub Copilot / TASK-007
 */
public class JobPublishedEvent extends JobDomainEvent {
    
    private final UUID employerId;
    private final JobTitle title;
    private final JobDescription description;
    private final JobLocation location;
    private final JobSalary salary;
    
    /**
     * Create a JobPublishedEvent with current timestamp.
     */
    public JobPublishedEvent(
            UUID jobId,
            UUID employerId,
            JobTitle title,
            JobDescription description,
            JobLocation location,
            JobSalary salary) {
        super(jobId);
        this.employerId = employerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
    }
    
    /**
     * Create a JobPublishedEvent with specific timestamp (for event sourcing).
     */
    public JobPublishedEvent(
            UUID eventId,
            UUID jobId,
            Instant occurredAt,
            UUID employerId,
            JobTitle title,
            JobDescription description,
            JobLocation location,
            JobSalary salary) {
        super(eventId, jobId, occurredAt);
        this.employerId = employerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
    }
    
    @Override
    public String getEventType() {
        return "JobPublished";
    }
    
    public UUID getEmployerId() {
        return employerId;
    }
    
    public JobTitle getTitle() {
        return title;
    }
    
    public JobDescription getDescription() {
        return description;
    }
    
    public JobLocation getLocation() {
        return location;
    }
    
    public JobSalary getSalary() {
        return salary;
    }
}
