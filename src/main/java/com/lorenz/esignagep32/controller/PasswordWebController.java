// src/main/java/com/lorenz/esignagep32/controller/PasswordWebController.java
package com.lorenz.esignagep32.controller;

import com.lorenz.esignagep32.dto.ChangePasswordDto;
import com.lorenz.esignagep32.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user password changes.
 */
@Controller
@RequiredArgsConstructor
public class PasswordWebController {
    private final UserService userService;

    /**
     * Displays the password change form.
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordForm", new ChangePasswordDto());
        return "change-password";
    }

    /**
     * Processes the submitted password change form.
     */
    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails user,
                                 @ModelAttribute("passwordForm") ChangePasswordDto form,
                                 Model model) {
        try {
            userService.changePassword(user.getUsername(), form);
            model.addAttribute("success", true);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "change-password";
    }
}
