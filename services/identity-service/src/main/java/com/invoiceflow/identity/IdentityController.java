package com.invoiceflow.identity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class IdentityController {
    private final JdbcTemplate jdbc;

    public IdentityController(JdbcTemplate j) {
        jdbc = j;
    }

    @GetMapping("/demo-users")
    public List<Map<String, Object>> users() {
        return jdbc.queryForList("SELECT user_id AS \"userId\",display_name AS \"displayName\",role,demo_only AS \"demoOnly\" FROM demo_users ORDER BY role");
    }

    @PostMapping("/demo-login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String id = body.getOrDefault("userId", "");
        return jdbc.queryForList("SELECT user_id AS \"userId\",display_name AS \"displayName\",role, true AS \"demoOnly\" FROM demo_users WHERE user_id=?", id).stream().findFirst().map(u -> Map.of("user", u, "accessToken", "POC-NOT-A-REAL-JWT", "warning", "Demo-only identity; not production authentication")).orElseThrow();
    }
}
