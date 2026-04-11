package com.akhil.urlShortner.controllers;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StringRedisTemplate redisTemplate;

    public AdminController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Existing logs endpoint...
    @GetMapping("/logs")
    public List<String> getLogs() {
        try {
            List<String> allLines = Files.readAllLines(Paths.get("app.log"));
            return allLines.stream().skip(Math.max(0, allLines.size() - 50)).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("Log file not found or empty.");
        }
    }

    // New: Immediate Cache Flush
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCache() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        return ResponseEntity.ok(Map.of("message", "All Redis caches cleared successfully."));
    }
}