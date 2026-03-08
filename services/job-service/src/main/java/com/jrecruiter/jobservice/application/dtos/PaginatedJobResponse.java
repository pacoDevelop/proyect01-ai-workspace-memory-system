package com.jrecruiter.jobservice.application.dtos;

import java.util.List;

/**
 * DTO: Paginated Job Response
 * 
 * Wrapper for paginated results with metadata.
 * 
 * @author GitHub Copilot / TASK-010
 */
public class PaginatedJobResponse {
    
    private List<JobResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // Constructors
    
    public PaginatedJobResponse() {
    }
    
    public PaginatedJobResponse(
            List<JobResponse> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }
    
    // Getters & Setters
    
    public List<JobResponse> getContent() {
        return content;
    }
    
    public void setContent(List<JobResponse> content) {
        this.content = content;
    }
    
    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
