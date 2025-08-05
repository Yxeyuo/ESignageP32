package com.lorenz.esignagep32.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller that handles application errors
 * by redirecting users based on authentication status.
 */
@Controller
public class CustomErrorController implements ErrorController {
    private static final String ERROR_PATH = "/error";

    /**
     * Handles all errors by checking if the user is authenticated.
     * Unauthenticated users are redirected to the login page,
     * authenticated users are redirected to the devices overview.
     *
     * @param request the HTTP request that triggered the error
     * @return redirect URL to the appropriate page
     */
    @RequestMapping(ERROR_PATH)
    public String handleError(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        boolean loggedIn = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        if (!loggedIn) {
            // Redirect unauthenticated users to login page
            return "redirect:/login";
        } else {
            // Redirect authenticated users to devices page
            return "redirect:/devices";
        }
    }
}
