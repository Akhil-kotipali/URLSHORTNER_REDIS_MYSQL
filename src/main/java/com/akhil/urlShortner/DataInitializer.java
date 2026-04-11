package com.akhil.urlShortner;

import com.akhil.urlShortner.models.SystemSettings;
import com.akhil.urlShortner.models.User;
import com.akhil.urlShortner.repositories.SystemSettingsRepository;
import com.akhil.urlShortner.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemSettingsRepository systemSettingsRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, SystemSettingsRepository systemSettingsRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.systemSettingsRepository=systemSettingsRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Automatically create the permanent admin if it doesn't exist
        if (userRepository.findByUsername("Akhil").isEmpty()) {
            User admin = new User();
            admin.setUsername("Akhil");
            admin.setPassword(passwordEncoder.encode("2170"));
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("Permanent Admin 'Akhil' configured.");
        }
        if (systemSettingsRepository.findById(1L).isEmpty()) {
    systemSettingsRepository.save(new SystemSettings());
    System.out.println("Default System Settings initialized.");
}
    }
}