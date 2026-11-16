package com.invoiceflow.risk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoiceflow.common.DomainEvent;
import com.invoiceflow.common.EventPublisher;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RiskConsumer {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final EventPublisher events;

    public RiskConsumer(JdbcTemplate j, ObjectMapper m, EventPublisher e) {
        jdbc = j;
        mapper = m;
        events = e;
    }

    @RabbitListener(queues = "risk.invoice-accepted")
    public void onAccepted(String raw) throws Exception {
        JsonNode n = mapper.readTree(raw);
        JsonNode p = n.path("payload");
        String id = p.path("invoiceId").asText();
        if (id.isBlank()) throw new IllegalArgumentException("invoiceId missing");
        if (jdbc.queryForObject("SELECT count(*) FROM risk_decisions WHERE invoice_id=?", Integer.class, id) > 0)
            return;
        BigDecimal amount = p.path("amount").decimalValue();
        String currency = p.path("currency").asText("INR");
        boolean eligible = amount.signum() > 0 && amount.compareTo(new BigDecimal("5000000")) <= 0 && "INR".equals(currency);
        int score = eligible ? 85 : 25;
        String reason = eligible ? "POC rules passed: accepted invoice, positive amount, INR, within demo limit" : "POC rules failed: check amount, currency or acceptance";
        jdbc.update("INSERT INTO risk_decisions(invoice_id,eligible,score,reason) VALUES(?,?,?,?)", id, eligible, score, reason);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("invoiceId", id);
        payload.put("supplierId", p.path("supplierId").asText());
        payload.put("buyerId", p.path("buyerId").asText());
        payload.put("amount", amount);
        payload.put("currency", currency);
        payload.put("eligible", eligible);
        payload.put("score", score);
        payload.put("reason", reason);
        events.publish("risk.completed", DomainEvent.of("risk.completed", id, "risk-service", payload));
    }

}
