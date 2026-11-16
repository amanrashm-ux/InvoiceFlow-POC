package com.invoiceflow.risk;
import java.util.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/risk") public class RiskController {private final JdbcTemplate jdbc;public RiskController(JdbcTemplate j){jdbc=j;}@GetMapping public List<Map<String,Object>> all(){return jdbc.queryForList("SELECT * FROM risk_decisions ORDER BY evaluated_at DESC");}}
