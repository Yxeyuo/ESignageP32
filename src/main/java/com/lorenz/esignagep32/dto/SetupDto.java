package com.lorenz.esignagep32.dto;

import lombok.Data;

/**
 * Data transfer object containing the initial setup configuration
 * for an ESignageP32 device.
 * <p>
 * Includes network settings, server domain, and registration identifiers
 * required for device provisioning.
 */
@Data
public class SetupDto {
    // Network connection parameters
    private String wifiSsid;
    private String wifiPassword;
    private String ntpServer;

    // Server endpoint configuration
    private String serverDomain;

    // Tokens for device provisioning
    private String registrationToken;
    private String deviceToken;

    // Device identifier assigned during registration
    private Long deviceId;
}
