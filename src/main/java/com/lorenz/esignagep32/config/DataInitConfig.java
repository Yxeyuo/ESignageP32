package com.lorenz.esignagep32.config;

import com.lorenz.esignagep32.model.User;
import com.lorenz.esignagep32.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-user.username}")
    private String defaultUsername;  // Default admin username from application properties

    @Value("${app.default-user.password}")
    private String defaultPassword;  // Default admin password from application properties

    /**
     * Creates a default admin user on application startup if none exists.
     */
    @Bean
    public CommandLineRunner createDefaultUser() {
        return args -> {
            // Check if the user table is empty
            if (userRepo.count() == 0) {
                User admin = new User();
                admin.setUsername(defaultUsername);
                // Encode the default password before saving
                admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
                admin.setRole("ROLE_ADMIN");
                userRepo.save(admin);
                // Log creation of default admin credentials
                System.out.printf("Default admin created: %s / %s%n", defaultUsername, defaultPassword);
            }
        };
    }
}
