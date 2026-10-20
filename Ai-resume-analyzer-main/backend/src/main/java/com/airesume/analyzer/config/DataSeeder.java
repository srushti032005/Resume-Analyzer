package com.airesume.analyzer.config;

import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.entity.enums.Role;
import com.airesume.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seed("admin@airesume.dev", "Admin User", "Admin@12345", Role.ADMIN);
        seed("demo@airesume.dev", "Demo User", "Demo@12345", Role.USER);
    }

    private void seed(String email, String name, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = User.builder()
                .fullName(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build();
        userRepository.save(user);
        log.info("Seeded {} account: {} / {}", role, email, rawPassword);
    }
}
