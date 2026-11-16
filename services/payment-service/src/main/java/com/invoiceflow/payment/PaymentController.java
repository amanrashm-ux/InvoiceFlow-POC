package com.invoiceflow.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final JdbcTemplate jdbc;

    public PaymentController(JdbcTemplate j) {
        jdbc = j;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return jdbc.queryForList("SELECT * FROM payments ORDER BY disbursed_at DESC");
    }
}
