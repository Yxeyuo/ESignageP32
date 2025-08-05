// src/main/java/com/lorenz/esignagep32/service/UserService.java
package com.lorenz.esignagep32.service;

import com.lorenz.esignagep32.dto.ChangePasswordDto;
import com.lorenz.esignagep32.model.User;
import com.lorenz.esignagep32.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user-related operations such as changing password.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Changes the password of the given user after verifying the current password.
     *
     * @param username the username of the user
     * @param form     the change-password form data
     * @throws IllegalArgumentException if the current password is invalid or new passwords do not match
     */
    public void changePassword(String username, ChangePasswordDto form) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepo.save(user);
    }
}
