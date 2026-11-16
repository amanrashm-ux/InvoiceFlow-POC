package com.invoiceflow.reconciliation;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
    @Bean
    Queue reconciliationpaymentdisbursedQueue() {
        return QueueBuilder.durable("reconciliation.payment-disbursed").withArgument("x-dead-letter-exchange", "invoiceflow.dlx").build();
    }

    @Bean
    Binding reconciliationpaymentdisbursedBinding(@Qualifier("reconciliationpaymentdisbursedQueue") Queue q, TopicExchange domainExchange) {
        return BindingBuilder.bind(q).to(domainExchange).with("payment.disbursed");
    }
}
