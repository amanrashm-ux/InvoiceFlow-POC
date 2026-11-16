package com.invoiceflow.identity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages="com.invoiceflow")
public class IdentityServiceApplication { public static void main(String[] args) { SpringApplication.run(IdentityServiceApplication.class,args); } }
