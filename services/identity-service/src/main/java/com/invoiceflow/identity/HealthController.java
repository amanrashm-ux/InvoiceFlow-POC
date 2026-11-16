package com.invoiceflow.identity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/internal/info")
    public Map<String, String> info() {
        return Map.of("service", "identity-service", "status", "UP");
    }
}
