package com.jrecruiter.jobservice.infrastructure.config;

import java.util.Collections;
import java.util.Map;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 * 
 * Configures message broker infrastructure:
 * - Exchanges (Fanout, Direct)
 * - Queues (main + dead-letter)
 * - Bindings
 * - Message converters
 */
@Configuration
public class RabbitMQConfig {

    // ============= MAIN EXCHANGES =============
    
    @Bean
    public FanoutExchange jobEventsExchange() {
        return new FanoutExchange("job-events", true, false);
    }

    // ============= MAIN QUEUES =============
    
    @Bean
    public Queue jobSearchQueue() {
        return QueueBuilder.durable("job-search-queue")
            .arguments(Map.of(
                "x-dead-letter-exchange", "job-search-dlq",
                "x-message-ttl", 86400000,
                "x-max-length", 100000
            ))
            .build();
    }

    @Bean
    public Queue jobNotificationQueue() {
        return QueueBuilder.durable("job-notification-queue")
            .arguments(Map.of(
                "x-dead-letter-exchange", "job-notification-dlq",
                "x-message-ttl", 86400000
            ))
            .build();
    }

    // ============= MAIN BINDINGS =============
    
    @Bean
    public Binding jobSearchBinding(Queue jobSearchQueue, FanoutExchange jobEventsExchange) {
        return BindingBuilder.bind(jobSearchQueue).to(jobEventsExchange);
    }

    @Bean
    public Binding jobNotificationBinding(Queue jobNotificationQueue, FanoutExchange jobEventsExchange) {
        return BindingBuilder.bind(jobNotificationQueue).to(jobEventsExchange);
    }

    // ============= DEAD-LETTER EXCHANGES & QUEUES =============
    
    @Bean
    public DirectExchange jobSearchDlqExchange() {
        return new DirectExchange("job-search-dlq", true, false);
    }

    @Bean
    public Queue jobSearchDlqQueue() {
        return QueueBuilder.durable("job-search-dlq-queue")
            .arguments(Map.of("x-message-ttl", 604800000))
            .build();
    }

    @Bean
    public Binding jobSearchDlqBinding(Queue jobSearchDlqQueue, DirectExchange jobSearchDlqExchange) {
        return BindingBuilder.bind(jobSearchDlqQueue).to(jobSearchDlqExchange).with("dead-letter");
    }

    @Bean
    public DirectExchange jobNotificationDlqExchange() {
        return new DirectExchange("job-notification-dlq", true, false);
    }

    @Bean
    public Queue jobNotificationDlqQueue() {
        return QueueBuilder.durable("job-notification-dlq-queue")
            .arguments(Map.of("x-message-ttl", 604800000))
            .build();
    }

    @Bean
    public Binding jobNotificationDlqBinding(Queue jobNotificationDlqQueue, DirectExchange jobNotificationDlqExchange) {
        return BindingBuilder.bind(jobNotificationDlqQueue).to(jobNotificationDlqExchange).with("dead-letter");
    }

    // ============= MESSAGE CONVERTER =============
    
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
