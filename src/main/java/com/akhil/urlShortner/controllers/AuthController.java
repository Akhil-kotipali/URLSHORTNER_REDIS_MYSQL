package com.akhil.urlShortner.controllers;

import com.akhil.urlShortner.models.User;
import com.akhil.urlShortner.repositories.UserRepository;
import com.akhil.urlShortner.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username taken"));
        }
        // First user becomes admin, rest are normal users
        user.setRole(userRepository.count() == 0 ? "ROLE_ADMIN" : "ROLE_USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User existingUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            String token = jwtUtil.generateToken(existingUser.getUsername(), existingUser.getRole());
            return ResponseEntity.ok(Map.of("token", token, "role", existingUser.getRole()));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
}