package com.invoiceflow.payment;
import org.springframework.amqp.core.*;import org.springframework.context.annotation.*;import org.springframework.beans.factory.annotation.Qualifier;
@Configuration public class QueueConfig {
  @Bean Queue paymentfinancingapprovedQueue(){return QueueBuilder.durable("payment.financing-approved").withArgument("x-dead-letter-exchange","invoiceflow.dlx").build();}
  @Bean Binding paymentfinancingapprovedBinding(@Qualifier("paymentfinancingapprovedQueue") Queue q,TopicExchange domainExchange){return BindingBuilder.bind(q).to(domainExchange).with("financing.approved");}
}
