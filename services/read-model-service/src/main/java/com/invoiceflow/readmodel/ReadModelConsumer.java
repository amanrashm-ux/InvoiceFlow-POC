package com.invoiceflow.readmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReadModelConsumer {
  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public ReadModelConsumer(JdbcTemplate jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  @RabbitListener(queues = "read-model.events")
  public void consume(String raw) throws Exception {
    JsonNode event = mapper.readTree(raw);
    try {
      jdbc.update("INSERT INTO domain_event_log(event_id,event_type,aggregate_id,occurred_at,payload) VALUES(?,?,?,?,?)",
          event.path("eventId").asText(),
          event.path("eventType").asText(),
          event.path("aggregateId").asText(),
          event.path("occurredAt").asText(),
          event.path("payload").toString());
    } catch (DuplicateKeyException ignored) {
      // RabbitMQ may redeliver; event_id is the idempotency key.
    }
  }
}
