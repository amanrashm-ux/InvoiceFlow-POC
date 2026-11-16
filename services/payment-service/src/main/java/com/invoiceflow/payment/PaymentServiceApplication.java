package com.invoiceflow.payment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages="com.invoiceflow")
public class PaymentServiceApplication { public static void main(String[] args) { SpringApplication.run(PaymentServiceApplication.class,args); } }
