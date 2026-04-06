package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Controller for Discovery Service
 * Provides custom health endpoints and service information
 */
@RestController
@RequestMapping("/api/v1/discovery")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    /**
     * Custom health check endpoint
     * @return Health status of the discovery service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", applicationName);
        health.put("port", serverPort);
        health.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(health);
    }

    /**
     * Service info endpoint
     * @return Information about the discovery service
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Discovery Service");
        info.put("description", "Eureka Service Discovery for Microservices Architecture");
        info.put("version", "1.0.0");
        info.put("documentation", "Access Eureka dashboard at /");

        return ResponseEntity.ok(info);
    }
}

