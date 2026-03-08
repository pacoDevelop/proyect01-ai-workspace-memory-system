package com.jrecruiter.searchservice.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "job-events";
    public static final String SEARCH_QUEUE = "job-search-queue";

    @Bean
    public FanoutExchange jobEventsExchange() {
        return new FanoutExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue searchQueue() {
        return new Queue(SEARCH_QUEUE);
    }

    @Bean
    public Binding bindingSearch(Queue searchQueue, FanoutExchange jobEventsExchange) {
        return BindingBuilder.bind(searchQueue).to(jobEventsExchange);
    }
}
