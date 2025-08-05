package com.lorenz.esignagep32.controller;

import com.lorenz.esignagep32.dto.SetupDto;
import com.lorenz.esignagep32.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST controller for providing setup configuration to ESignageP32 clients.
 * <p>
 * Exposes an endpoint that returns a SetupDto containing
 * the necessary configuration for a device based on the authenticated user.
 */
@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupConfigController {

    private final RegistrationService registrationService;  // Service to generate setup configurations

    /**
     * Returns the setup configuration for the current authenticated user.
     *
     * @param principal security principal representing the authenticated user
     * @return a SetupDto containing the device setup configuration
     */
    @GetMapping
    public SetupDto getSetupConfig(Principal principal) {
        return registrationService.generateSetupConfig(principal.getName());
    }
}
