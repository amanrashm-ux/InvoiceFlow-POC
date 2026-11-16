package com.invoiceflow.financing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.invoiceflow")
public class FinancingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinancingServiceApplication.class, args);
    }
}
