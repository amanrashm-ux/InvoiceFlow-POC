package com.invoiceflow.readmodel;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class ReadModelController {
    private final JdbcTemplate jdbc;

    public ReadModelController(JdbcTemplate j) {
        jdbc = j;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("eventCount", jdbc.queryForObject("SELECT count(*) FROM domain_event_log", Long.class));
        m.put("invoiceCreated", count("invoice.created"));
        m.put("invoiceAccepted", count("invoice.accepted"));
        m.put("riskCompleted", count("risk.completed"));
        m.put("financingApproved", count("financing.approved"));
        m.put("paymentsDisbursed", count("payment.disbursed"));
        m.put("reconciliationsCompleted", count("reconciliation.completed"));
        m.put("recentEvents", jdbc.queryForList("SELECT event_id AS \"eventId\",event_type AS \"eventType\",aggregate_id AS \"aggregateId\",occurred_at AS \"occurredAt\",received_at AS \"receivedAt\" FROM domain_event_log ORDER BY received_at DESC LIMIT 15"));
        return m;
    }

    private long count(String t) {
        return jdbc.queryForObject("SELECT count(*) FROM domain_event_log WHERE event_type=?", Long.class, t);
    }
}
