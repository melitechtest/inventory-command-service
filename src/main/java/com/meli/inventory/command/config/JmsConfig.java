package com.meli.inventory.command.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;

@EnableJms
@Configuration
public class JmsConfig {

    /**
     * JSON -> TEXT converter with a standard Spring "__TypeId__" header.
     * This causes the producer to set "__TypeId__" to the FQN of the payload class.
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("__TypeId__");
        return converter;
    }

    /**
     * JmsTemplate configured to publish to Topics (Pub/Sub) and using the JSON converter.
     * The ConnectionFactory is autoconfigured by Spring from spring.activemq.*.
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setPubSubDomain(true);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
