package com.invoiceflow.common;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@Component
public class EventPublisher {
  private final RabbitTemplate rabbit; private final ObjectMapper mapper;
  public EventPublisher(RabbitTemplate rabbit, ObjectMapper mapper){ this.rabbit=rabbit; this.mapper=mapper; }
  public void publish(String routingKey, DomainEvent event){
    try { rabbit.convertAndSend(MessagingConfig.EXCHANGE, routingKey, mapper.writeValueAsString(event)); }
    catch(Exception e){ throw new IllegalStateException("Could not serialize domain event",e); }
  }
}
