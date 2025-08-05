package com.lorenz.esignagep32.service;

import com.lorenz.esignagep32.dto.SetupDto;
import com.lorenz.esignagep32.model.GlobalSettings;
import com.lorenz.esignagep32.model.RegistrationToken;
import com.lorenz.esignagep32.model.User;
import com.lorenz.esignagep32.repository.RegistrationTokenRepository;
import com.lorenz.esignagep32.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for handling device registration and setup configuration generation
 * for ESignageP32 devices.
 * <p>
 * Creates device entries, registration tokens, and populates SetupDto
 * with required provisioning information.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final RegistrationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final GlobalSettingsService settingsService;
    private final DeviceService deviceService;

    /**
     * Generates the initial setup configuration for a new device.
     * <ol>
     *   <li>Validates and retrieves the user by username</li>
     *   <li>Loads current global settings</li>
     *   <li>Creates a new device record with default naming</li>
     *   <li>Generates and persists a registration token</li>
     *   <li>Populates and returns a SetupDto with provisioning data</li>
     * </ol>
     *
     * @param username the name of the user requesting setup
     * @return SetupDto containing WiFi, NTP, server domain, registration token,
     *         device ID, and device token
     */
    public SetupDto generateSetupConfig(String username) {
        // 1) Retrieve user or throw if not found
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + username));

        // 2) Load global settings (WiFi, NTP, server domain)
        GlobalSettings gs = settingsService.getSettings();

        // 3) Create a new device with a default generated name
        var device = deviceService.createDevice(username,
                "ESP32-" + UUID.randomUUID());

        // 4) Generate and save a new registration token
        String registrationToken = UUID.randomUUID().toString();
        RegistrationToken reg = new RegistrationToken();
        reg.setToken(registrationToken);
        reg.setUser(user);
        reg.setCreatedAt(Instant.now());
        tokenRepo.save(reg);

        // 5) Build and return the setup configuration DTO
        SetupDto dto = new SetupDto();
        dto.setWifiSsid(gs.getWifiSsid());
        dto.setWifiPassword(gs.getWifiPassword());
        dto.setNtpServer(gs.getNtpServer());
        dto.setServerDomain(gs.getServerDomain());
        dto.setRegistrationToken(registrationToken);
        dto.setDeviceId(device.getId());
        dto.setDeviceToken(device.getDeviceToken());
        return dto;
    }
}
