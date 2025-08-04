package com.andsantos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class Rinhabackend2025Application {

    public static void main(String[] args) {
        SpringApplication.run(Rinhabackend2025Application.class, args);
    }

}
