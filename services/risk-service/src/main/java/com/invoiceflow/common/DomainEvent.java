package com.invoiceflow.common;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
public record DomainEvent(String eventId, String eventType, int eventVersion, String aggregateId,
                          Instant occurredAt, String correlationId, String producer, Map<String,Object> payload) {
  public static DomainEvent of(String type, String aggregateId, String producer, Map<String,Object> payload) {
    return new DomainEvent(UUID.randomUUID().toString(), type, 1, aggregateId, Instant.now(),
      UUID.randomUUID().toString(), producer, payload);
  }
}
