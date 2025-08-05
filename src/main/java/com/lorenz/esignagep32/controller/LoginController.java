package com.lorenz.esignagep32.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller handling login and logout flow for ESignageP32 application.
 */
@Controller
public class LoginController {

    /**
     * Serves the login page.
     *
     * @return name of the login view template
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Logs out the current user by invalidating the session and clearing cookies,
     * then redirects to the login page with a logout indicator.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     * @return redirect URL to the login page with logout query
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate the HTTP session
        request.getSession().invalidate();

        // Clear all cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        // Redirect to Spring Security logout handler and then to login page
        return "redirect:/login?logout";
    }
}
