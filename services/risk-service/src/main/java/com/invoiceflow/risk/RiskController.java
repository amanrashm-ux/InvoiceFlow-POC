package com.invoiceflow.risk;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/risk")
public class RiskController {
    private final JdbcTemplate jdbc;

    public RiskController(JdbcTemplate j) {
        jdbc = j;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return jdbc.queryForList("SELECT * FROM risk_decisions ORDER BY evaluated_at DESC");
    }
}
