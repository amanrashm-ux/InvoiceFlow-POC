package com.invoiceflow.financing;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/financing")
public class FinancingController {
    private final JdbcTemplate jdbc;

    public FinancingController(JdbcTemplate j) {
        jdbc = j;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return jdbc.queryForList("SELECT * FROM financing_applications ORDER BY created_at DESC");
    }
}
