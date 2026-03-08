package com.jrecruiter.jobservice.infrastructure.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrecruiter.jobservice.application.dtos.CreateJobRequest;
import com.jrecruiter.jobservice.application.dtos.JobResponse;
import com.jrecruiter.jobservice.application.dtos.PaginatedJobResponse;
import com.jrecruiter.jobservice.application.services.JobApplicationService;

/**
 * Integration Test: Job REST Controller
 * 
 * Tests REST endpoints using MockMvc (Spring Test).
 * Mocks the application service dependency.
 * 
 * @author GitHub Copilot / TASK-011
 */
@WebMvcTest(JobController.class)
@DisplayName("Job REST Controller Integration Tests")
class JobControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private JobApplicationService jobApplicationService;
    
    private UUID jobId;
    private UUID employerId;
    private JobResponse mockJobResponse;
    
    @BeforeEach
    void setUp() {
        jobId = UUID.randomUUID();
        employerId = UUID.randomUUID();
        
        mockJobResponse = new JobResponse(
                jobId,
                "JOB-2026-001",
                employerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Senior Java Engineer",
                "Build microservices with Spring Boot",
                "TechCorp Inc",
                new JobResponse.LocationResponse(
                        "123 Tech St",
                        "San Francisco",
                        "CA",
                        "94102",
                        "United States",
                        "US",
                        37.78,
                        -122.41,
                        false
                ),
                new JobResponse.SalaryResponse(
                        new BigDecimal("150000"),
                        new BigDecimal("200000"),
                        "USD",
                        "ANNUAL"
                ),
                "EMPLOYER",
                "DRAFT",
                Instant.now(),
                null,
                null,
                Instant.now()
        );
    }
    
    @Test
    @DisplayName("POST /api/jobs creates job and returns 201")
    void testCreateJob() throws Exception {
        CreateJobRequest request = new CreateJobRequest(
                "Senior Java Engineer",
                "Build microservices with Spring Boot and modern cloud technologies",
                "TechCorp Inc",
                new CreateJobRequest.LocationRequest(
                        "123 Tech St",
                        "San Francisco",
                        "CA",
                        "94102",
                        "United States",
                        "US",
                        false
                ),
                new CreateJobRequest.SalaryRequest(
                        new BigDecimal("150000"),
                        new BigDecimal("200000"),
                        "USD",
                        "ANNUAL"
                ),
                "EMPLOYER",
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        
        when(jobApplicationService.createJob(any(CreateJobRequest.class), any(UUID.class)))
                .thenReturn(mockJobResponse);
        
        mockMvc.perform(post("/api/jobs")
                .header("X-Employer-ID", employerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").exists())
                .andExpect(jsonPath("$.title").value("Senior Java Engineer"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
    
    @Test
    @DisplayName("GET /api/jobs/{id} returns job (200 OK)")
    void testGetJobById() throws Exception {
        when(jobApplicationService.getJobById(jobId)).thenReturn(mockJobResponse);
        
        mockMvc.perform(get("/api/jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.title").value("Senior Java Engineer"))
                .andExpect(jsonPath("$.companyName").value("TechCorp Inc"));
    }
    
    @Test
    @DisplayName("GET /api/jobs/{id} returns 404 when not found")
    void testGetJobByIdNotFound() throws Exception {
        when(jobApplicationService.getJobById(jobId))
                .thenThrow(new java.util.NoSuchElementException("Job not found"));
        
        mockMvc.perform(get("/api/jobs/{id}", jobId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("GET /api/jobs returns paginated published jobs (200 OK)")
    void testListPublishedJobs() throws Exception {
        PaginatedJobResponse paginatedResponse = new PaginatedJobResponse(
                java.util.List.of(mockJobResponse),
                0,
                20,
                1,
                1,
                false,
                false
        );
        
        when(jobApplicationService.listPublishedJobs(0, 20)).thenReturn(paginatedResponse);
        
        mockMvc.perform(get("/api/jobs?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }
    
    @Test
    @DisplayName("POST /api/jobs/{id}/publish publishes job (200 OK)")
    void testPublishJob() throws Exception {
        mockJobResponse.setStatus("PUBLISHED");
        mockJobResponse.setPublishedAt(Instant.now());
        
        when(jobApplicationService.publishJob(jobId)).thenReturn(mockJobResponse);
        
        mockMvc.perform(post("/api/jobs/{id}/publish", jobId)
                .header("X-Employer-ID", employerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").exists());
    }
    
    @Test
    @DisplayName("POST /api/jobs/{id}/close closes job (200 OK)")
    void testCloseJob() throws Exception {
        mockJobResponse.setStatus("CLOSED");
        
        JobController.CloseJobRequest closeRequest = new JobController.CloseJobRequest();
        closeRequest.setReason("Position filled");
        
        when(jobApplicationService.closeJob(eq(jobId), anyString())).thenReturn(mockJobResponse);
        
        mockMvc.perform(post("/api/jobs/{id}/close", jobId)
                .header("X-Employer-ID", employerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(closeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
    
    @Test
    @DisplayName("DELETE /api/jobs/{id} deletes draft job (204 No Content)")
    void testDeleteJob() throws Exception {
        doNothing().when(jobApplicationService).deleteJob(jobId);
        
        mockMvc.perform(delete("/api/jobs/{id}", jobId)
                .header("X-Employer-ID", employerId.toString()))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("PUT /api/jobs/{id} updates job (200 OK)")
    void testUpdateJob() throws Exception {
        CreateJobRequest.LocationRequest locationRequest = new CreateJobRequest.LocationRequest(
                "123 Tech St",
                "San Francisco",
                "CA",
                "94102",
                "United States",
                "US",
                false
        );
        
        mockJobResponse.setTitle("Updated Title");
        
        when(jobApplicationService.updateJob(eq(jobId), any())).thenReturn(mockJobResponse);
        
        mockMvc.perform(put("/api/jobs/{id}", jobId)
                .header("X-Employer-ID", employerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("POST /api/jobs requires X-Employer-ID header")
    void testCreateJobMissingEmployerHeader() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        
        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
