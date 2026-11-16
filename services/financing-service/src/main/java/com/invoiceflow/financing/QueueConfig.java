package com.invoiceflow.financing;
import org.springframework.amqp.core.*;import org.springframework.context.annotation.*;import org.springframework.beans.factory.annotation.Qualifier;
@Configuration public class QueueConfig {
  @Bean Queue financingriskcompletedQueue(){return QueueBuilder.durable("financing.risk-completed").withArgument("x-dead-letter-exchange","invoiceflow.dlx").build();}
  @Bean Binding financingriskcompletedBinding(@Qualifier("financingriskcompletedQueue") Queue q,TopicExchange domainExchange){return BindingBuilder.bind(q).to(domainExchange).with("risk.completed");}
}
