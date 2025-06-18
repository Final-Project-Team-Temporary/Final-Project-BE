package com.example.whiplash.global;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/my-health")
    public String healthCheck() {
        return "OK";
    }
}
