package com.jrecruiter.notificationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Notification Service Application Entry Point.
 * 
 * This microservice is responsible for:
 * - Consuming events from RabbitMQ (JobCreated, ApplicationReceived, etc.)
 * - Processing and formatting notifications
 * - Sending emails asynchronously via SMTP
 * - Storing notification history and status
 * 
 * Key features:
 * - Event-driven asynchronous processing
 * - Thymeleaf templating for email content
 * - RabbitMQ integration with manual acknowledgment
 * - Spring Security with JWT validation
 * - Prometheus metrics for monitoring
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        log.info("Starting Notification Service Application...");
        SpringApplication.run(NotificationServiceApplication.class, args);
        log.info("Notification Service started successfully");
    }

}
