package com.jrecruiter.searchservice.domain.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Elasticsearch Document: Job Search Index
 * Searchable representation of job postings.
 * 
 * @author GitHub Copilot / TASK-016
 */
@Document(indexName = "jobs", createIndex = true)
public class JobSearchDocument {
    
    @Id
    private String jobId;
    
    @Field(type = FieldType.Keyword)
    private String universalId;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String companyName;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Geo_point)
    private String location; // geo_point format
    
    @Field(type = FieldType.Double)
    private Double minSalary;
    
    @Field(type = FieldType.Double)
    private Double maxSalary;
    
    @Field(type = FieldType.Keyword)
    private String currency;
    
    @Field(type = FieldType.Keyword)
    private String industryId;
    
    @Field(type = FieldType.Keyword)
    private String regionId;
    
    @Field(type = FieldType.Boolean)
    private Boolean remote;
    
    @Field(type = FieldType.Date)
    private LocalDateTime publishedAt;
    
    @Field(type = FieldType.Date)
    private LocalDateTime indexedAt;
    
    public JobSearchDocument() {}
    
    // Getters/Setters (abbreviated for space)
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getMinSalary() { return minSalary; }
    public void setMinSalary(Double minSalary) { this.minSalary = minSalary; }
    
    public Double getMaxSalary() { return maxSalary; }
    public void setMaxSalary(Double maxSalary) { this.maxSalary = maxSalary; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Boolean getRemote() { return remote; }
    public void setRemote(Boolean remote) { this.remote = remote; }
    
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
