package com.invoiceflow.readmodel;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReadModelQueueConfig {
    @Bean
    Queue readModelQueue() {
        return QueueBuilder.durable("read-model.events").withArgument("x-dead-letter-exchange", "invoiceflow.dlx").build();
    }

    @Bean
    Binding readModelBinding(@Qualifier("readModelQueue") Queue readModelQueue, TopicExchange domainExchange) {
        return BindingBuilder.bind(readModelQueue).to(domainExchange).with("#");
    }
}
