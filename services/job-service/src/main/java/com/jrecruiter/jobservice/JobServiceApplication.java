package com.jrecruiter.jobservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Job Service - Main Spring Boot Application
 * 
 * Microservice for Job management in JRecruiter platform.
 * Handles job creation, lifecycle, and search operations.
 */
@SpringBootApplication
@EnableScheduling
public class JobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}
