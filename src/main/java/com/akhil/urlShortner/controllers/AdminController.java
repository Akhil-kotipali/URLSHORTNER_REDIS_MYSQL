package com.akhil.urlShortner.controllers;

import com.akhil.urlShortner.models.SystemSettings;
import com.akhil.urlShortner.models.Url;
import com.akhil.urlShortner.models.User;
import com.akhil.urlShortner.repositories.SystemSettingsRepository;
import com.akhil.urlShortner.repositories.UrlRepository;
import com.akhil.urlShortner.repositories.UserRepository;
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
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final SystemSettingsRepository systemSettingsRepository;

    public AdminController(StringRedisTemplate redisTemplate, UserRepository userRepository, UrlRepository urlRepository, SystemSettingsRepository systemSettingsRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.urlRepository = urlRepository;
        this.systemSettingsRepository=systemSettingsRepository;
    }

    // --- SYSTEM TELEMETRY ---
    @GetMapping("/logs")
    public List<String> getLogs() {
        try {
            List<String> allLines = Files.readAllLines(Paths.get("app.log"));
            return allLines.stream().skip(Math.max(0, allLines.size() - 100)).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("Log file not found or empty.");
        }
    }

    @PostMapping("/clear-cache/{type}")
    public ResponseEntity<?> clearCacheType(@PathVariable String type) {
        try {
            if ("all".equals(type)) {
                redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
                return ResponseEntity.ok(Map.of("message", "CRITICAL: Entire Redis Database Flushed."));
            } else if ("urls".equals(type)) {
                redisTemplate.delete(redisTemplate.keys("urls::*"));
                return ResponseEntity.ok(Map.of("message", "Standard User URL cache cleared."));
            } else if ("admin".equals(type)) {
                redisTemplate.delete(redisTemplate.keys("admin-urls::*"));
                return ResponseEntity.ok(Map.of("message", "Custom Admin URL cache cleared."));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown cache type"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Redis error: " + e.getMessage()));
        }
    }

    // --- USER CRUD MANAGEMENT ---
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if(id == 1) return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete the primary admin."));
        
        // Delete all URLs owned by this user first to prevent foreign key errors
        User user = userRepository.findById(id).orElseThrow();
        List<Url> userUrls = urlRepository.findByUser(user);
        urlRepository.deleteAll(userUrls);
        
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User and all associated URLs deleted."));
    }

    // --- INDIVIDUAL USER URL MANAGEMENT ---
    @GetMapping("/users/{id}/urls")
    public ResponseEntity<List<Url>> getUserUrls(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(urlRepository.findByUser(user));
    }

    @DeleteMapping("/urls/{urlId}")
    public ResponseEntity<?> deleteUrl(@PathVariable Long urlId) {
        urlRepository.deleteById(urlId);
        return ResponseEntity.ok(Map.of("message", "URL successfully deleted."));
    }

    @GetMapping("/settings")
public ResponseEntity<SystemSettings> getSettings() {
    return ResponseEntity.ok(systemSettingsRepository.findById(1L).orElseThrow());
}

@PostMapping("/settings")
public ResponseEntity<?> updateSettings(@RequestBody SystemSettings newSettings) {
    SystemSettings settings = systemSettingsRepository.findById(1L).orElseThrow();
    settings.setDefaultExpiryHours(newSettings.getDefaultExpiryHours());
    settings.setDefaultMaxTaps(newSettings.getDefaultMaxTaps());
    settings.setMaxLinksPerUser(newSettings.getMaxLinksPerUser());
    systemSettingsRepository.save(settings);
    return ResponseEntity.ok(Map.of("message", "Global settings updated successfully."));
}
}