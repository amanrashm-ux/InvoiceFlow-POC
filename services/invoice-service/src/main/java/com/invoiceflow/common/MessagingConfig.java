package com.invoiceflow.common;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    public static final String EXCHANGE = "invoiceflow.domain";

    @Bean
    TopicExchange domainExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange("invoiceflow.dlx", true, false);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable("invoiceflow.dead-letter").build();
    }

    @Bean
    Binding deadLetterBinding(@Qualifier("deadLetterQueue") Queue q) {
        return BindingBuilder.bind(q).to(deadLetterExchange()).with("#");
    }

    @Bean
    Queue analyticsQueue() {
        return QueueBuilder.durable("analytics.domain-events").withArgument("x-dead-letter-exchange", "invoiceflow.dlx").build();
    }

    @Bean
    Binding analyticsBinding(TopicExchange domainExchange, @Qualifier("analyticsQueue") Queue q) {
        return BindingBuilder.bind(q).to(domainExchange).with("#");
    }
}
