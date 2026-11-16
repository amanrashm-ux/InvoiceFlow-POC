package com.invoiceflow.reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceflow.common.DomainEvent;
import com.invoiceflow.common.EventPublisher;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ReconciliationConsumer {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final EventPublisher events;

    public ReconciliationConsumer(JdbcTemplate j, ObjectMapper m, EventPublisher e) {
        jdbc = j;
        mapper = m;
        events = e;
    }

    @RabbitListener(queues = "reconciliation.payment-disbursed")
    public void onPayment(String raw) throws Exception {
        JsonNode n = mapper.readTree(raw), p = n.path("payload");
        String pay = p.path("paymentId").asText();
        if (pay.isBlank()) throw new IllegalArgumentException("paymentId missing");
        if (jdbc.queryForObject("SELECT count(*) FROM reconciliations WHERE payment_id=?", Integer.class, pay) > 0)
            return;
        String id = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        var amount = p.path("amount").decimalValue();
        String invoice = p.path("invoiceId").asText();
        jdbc.update("INSERT INTO reconciliations(reconciliation_id,payment_id,invoice_id,financing_id,matched_amount,currency,match_status,reason) VALUES(?,?,?,?,?,?,?,?)", id, pay, invoice, p.path("financingId").asText(), amount, p.path("currency").asText("INR"), "MATCHED", "POC match on payment event invoice reference, amount and currency");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reconciliationId", id);
        out.put("paymentId", pay);
        out.put("invoiceId", invoice);
        out.put("matchedAmount", amount);
        out.put("currency", p.path("currency").asText("INR"));
        out.put("matchStatus", "MATCHED");
        events.publish("reconciliation.completed", DomainEvent.of("reconciliation.completed", id, "reconciliation-service", out));
    }
}
