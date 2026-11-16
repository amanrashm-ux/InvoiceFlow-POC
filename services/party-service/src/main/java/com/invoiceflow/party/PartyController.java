package com.invoiceflow.party;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parties")
@CrossOrigin(origins = "*")
public class PartyController {
    private final JdbcTemplate jdbc;

    public PartyController(JdbcTemplate j) {
        jdbc = j;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return jdbc.queryForList("SELECT party_id AS \"partyId\",party_type AS \"partyType\",legal_name AS \"legalName\",email,country_code AS \"countryCode\",status FROM parties ORDER BY party_type,legal_name");
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String id) {
        return jdbc.queryForList("SELECT party_id AS \"partyId\",party_type AS \"partyType\",legal_name AS \"legalName\",email,country_code AS \"countryCode\",status FROM parties WHERE party_id=?", id).stream().findFirst().orElseThrow();
    }
}
