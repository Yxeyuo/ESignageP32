package com.lorenz.esignagep32.dto;

import lombok.Data;
import java.util.List;

/**
 * Data transfer object representing the full configuration
 * required by an ESignageP32 device on startup.
 * <p>
 * Contains network settings, device identifiers, timing intervals,
 * and any preloaded messages to display.
 */
@Data
public class ConfigDto {
    // Network connection parameters
    private String wifiSsid;
    private String wifiPassword;
    private String ntpServer;
    private String serverDomain;

    // Device identification
    private Long deviceId;
    private String deviceToken;

    // Timing settings for device operations
    private int updateIntervalSeconds;
    private int rotateIntervalSeconds;

    // Preloaded messages to display on the device
    private List<MessageDto> messages;
}