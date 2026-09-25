package com.saidazi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SaidaziApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaidaziApplication.class, args);
    }
}
