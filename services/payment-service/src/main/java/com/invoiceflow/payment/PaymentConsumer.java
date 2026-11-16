package com.invoiceflow.payment;

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
public class PaymentConsumer {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final EventPublisher events;

    public PaymentConsumer(JdbcTemplate j, ObjectMapper m, EventPublisher e) {
        jdbc = j;
        mapper = m;
        events = e;
    }

    @RabbitListener(queues = "payment.financing-approved")
    public void onApproval(String raw) throws Exception {
        JsonNode n = mapper.readTree(raw), p = n.path("payload");
        String fin = p.path("financingId").asText();
        if (fin.isBlank()) throw new IllegalArgumentException("financingId missing");
        if (jdbc.queryForObject("SELECT count(*) FROM payments WHERE financing_id=?", Integer.class, fin) > 0) return;
        String id = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String ref = "SIM-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        var amount = p.path("approvedAmount").decimalValue();
        String currency = p.path("currency").asText("INR");
        jdbc.update("INSERT INTO payments(payment_id,financing_id,invoice_id,amount,currency,payment_reference,status) VALUES(?,?,?,?,?,?,?)", id, fin, p.path("invoiceId").asText(), amount, currency, ref, "SIMULATED_DISBURSED");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("paymentId", id);
        out.put("financingId", fin);
        out.put("invoiceId", p.path("invoiceId").asText());
        out.put("amount", amount);
        out.put("currency", currency);
        out.put("paymentReference", ref);
        out.put("status", "SIMULATED_DISBURSED");
        events.publish("payment.disbursed", DomainEvent.of("payment.disbursed", id, "payment-service", out));
    }
}
