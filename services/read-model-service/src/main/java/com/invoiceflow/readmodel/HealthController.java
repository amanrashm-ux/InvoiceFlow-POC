package com.invoiceflow.readmodel;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController
public class HealthController {
  @GetMapping("/internal/info") public Map<String,String> info() { return Map.of("service","read-model-service","status","UP"); }
}
