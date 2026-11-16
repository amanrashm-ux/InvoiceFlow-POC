package com.invoiceflow.reconciliation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reconciliations")
public class ReconciliationController {
    private final JdbcTemplate jdbc;

    public ReconciliationController(JdbcTemplate j) {
        jdbc = j;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return jdbc.queryForList("SELECT * FROM reconciliations ORDER BY reconciled_at DESC");
    }
}
