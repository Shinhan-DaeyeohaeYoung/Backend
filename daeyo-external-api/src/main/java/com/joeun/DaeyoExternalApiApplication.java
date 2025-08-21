package com.joeun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DaeyoExternalApiApplication {
    public static void main(String[] args) {
        System.setProperty("spring.config.location", "classpath:/application-rds.yml,classpath:/application-redis.yml" +
                ",classpath:/application-infra.yml,classpath:/");
        SpringApplication.run(DaeyoExternalApiApplication.class, args);
    }
}