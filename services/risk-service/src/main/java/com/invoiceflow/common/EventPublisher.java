package com.invoiceflow.common;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    private final RabbitTemplate rabbit;

    public EventPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    public void publish(String routingKey, DomainEvent event) {
        rabbit.convertAndSend(MessagingConfig.EXCHANGE, routingKey, event);
    }
}
