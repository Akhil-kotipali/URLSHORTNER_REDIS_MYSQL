package com.akhil.urlShortner.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/logs")
    public List<String> getLogs() {
        try {
            // Reads the last 50 lines of the log file
            List<String> allLines = Files.readAllLines(Paths.get("app.log"));
            return allLines.stream().skip(Math.max(0, allLines.size() - 50)).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("Log file not found or empty.");
        }
    }
}