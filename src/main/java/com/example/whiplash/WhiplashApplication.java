package com.example.whiplash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WhiplashApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhiplashApplication.class, args);
    }

}