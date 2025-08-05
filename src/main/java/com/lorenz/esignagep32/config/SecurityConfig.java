package com.lorenz.esignagep32.config;

import com.lorenz.esignagep32.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for ESignageP32 application.
 *
 * Sets up password encoding, user details service, and security filter chain.
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides a BCrypt password encoder for hashing user passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Loads user-specific data for authentication from the UserRepository.
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPasswordHash())
                        .roles(u.getRole().replace("ROLE_", ""))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    /**
     * Excludes static resources (CSS, JS, images, WebJars) from security filtering.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**"
                );
    }

    /**
     * Configures the security filter chain, including CSRF protection,
     * authorization rules, form-based login, and logout handling.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Enable default CSRF protection
                .csrf(Customizer.withDefaults())

                // Define authorization rules for HTTP requests
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible endpoints
                        .requestMatchers(
                                HttpMethod.GET,
                                "/login",
                                "/flash.html",
                                "/js/flash.js",
                                "/firmware/**",
                                "/api/setup",
                                "/api/config/**"
                        ).permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Configure login form settings
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/devices", true)
                        .permitAll()
                )

                // Configure logout behavior
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
