package com.invoiceflow.risk;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
    @Bean
    Queue riskinvoiceacceptedQueue() {
        return QueueBuilder.durable("risk.invoice-accepted").withArgument("x-dead-letter-exchange", "invoiceflow.dlx").build();
    }

    @Bean
    Binding riskinvoiceacceptedBinding(@Qualifier("riskinvoiceacceptedQueue") Queue q, TopicExchange domainExchange) {
        return BindingBuilder.bind(q).to(domainExchange).with("invoice.accepted");
    }
}
