package com.meli.inventory.command.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the RabbitMQ infrastructure (Exchange, Queue, Binding).
 * This service (the producer) is responsible for declaring these elements.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "stock-exchange";
    public static final String ROUTING_KEY = "stock.update";
    public static final String QUEUE_NAME = "stock-query-queue";

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    Queue queue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}