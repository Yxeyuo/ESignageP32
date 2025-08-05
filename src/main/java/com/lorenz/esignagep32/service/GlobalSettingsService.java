package com.lorenz.esignagep32.service;

import com.lorenz.esignagep32.model.GlobalSettings;
import com.lorenz.esignagep32.repository.GlobalSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for retrieving and updating global settings for ESignageP32 devices.
 * <p>
 * Provides methods to load persisted settings or initialize defaults,
 * and to save updated settings.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GlobalSettingsService {

    private final GlobalSettingsRepository repo;

    @Value("${esp32.wifi.ssid}")
    private String defaultSsid;     // Default WiFi SSID from application properties

    @Value("${esp32.wifi.password}")
    private String defaultPassword; // Default WiFi password from application properties

    @Value("${esp32.ntp.server}")
    private String defaultNtp;      // Default NTP server from application properties

    @Value("${server.domain}")
    private String defaultDomain;   // Default server domain from application properties

    /**
     * Retrieves the persisted global settings.
     * If no settings exist, creates and saves a new entry
     * with default values.
     *
     * @return the GlobalSettings entity
     */
    @Transactional(readOnly = true)
    public GlobalSettings getSettings() {
        return repo.findById(1L)
                .orElseGet(() -> {
                    GlobalSettings settings = new GlobalSettings();
                    settings.setWifiSsid(defaultSsid);
                    settings.setWifiPassword(defaultPassword);
                    settings.setNtpServer(defaultNtp);
                    settings.setServerDomain(defaultDomain);
                    // Save and return new settings entry
                    return repo.save(settings);
                });
    }

    /**
     * Saves the provided global settings, overwriting the singleton entry.
     *
     * @param settings the GlobalSettings object populated from the form
     */
    public void updateSettings(GlobalSettings settings) {
        settings.setId(1L); // Ensure singleton ID
        repo.save(settings);
    }
}
