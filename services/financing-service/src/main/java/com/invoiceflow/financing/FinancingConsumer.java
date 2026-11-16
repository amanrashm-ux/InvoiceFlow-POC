package com.invoiceflow.financing;

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
import java.util.UUID;

@Component
public class FinancingConsumer {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final EventPublisher events;

    public FinancingConsumer(JdbcTemplate j, ObjectMapper m, EventPublisher e) {
        jdbc = j;
        mapper = m;
        events = e;
    }

    @RabbitListener(queues = "financing.risk-completed")
    public void onRisk(String raw) throws Exception {
        JsonNode n = mapper.readTree(raw), p = n.path("payload");
        String inv = p.path("invoiceId").asText();
        if (inv.isBlank()) throw new IllegalArgumentException("invoiceId missing");
        if (jdbc.queryForObject("SELECT count(*) FROM financing_applications WHERE invoice_id=?", Integer.class, inv) > 0)
            return;
        boolean eligible = p.path("eligible").asBoolean(false);
        BigDecimal requested = p.path("amount").decimalValue();
        BigDecimal approved = eligible ? requested.multiply(new BigDecimal("0.90")).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        String id = "FIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String status = eligible ? "APPROVED" : "REJECTED";
        jdbc.update("INSERT INTO financing_applications(financing_id,invoice_id,supplier_id,buyer_id,requested_amount,approved_amount,discount_rate,currency,status) VALUES(?,?,?,?,?,?,?,?,?)", id, inv, p.path("supplierId").asText(), p.path("buyerId").asText(), requested, approved, new BigDecimal("0.02"), p.path("currency").asText("INR"), status);
        if (eligible) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("financingId", id);
            out.put("invoiceId", inv);
            out.put("supplierId", p.path("supplierId").asText());
            out.put("buyerId", p.path("buyerId").asText());
            out.put("approvedAmount", approved);
            out.put("requestedAmount", requested);
            out.put("discountRate", 0.02);
            out.put("currency", p.path("currency").asText("INR"));
            events.publish("financing.approved", DomainEvent.of("financing.approved", id, "financing-service", out));
        }
    }
}
