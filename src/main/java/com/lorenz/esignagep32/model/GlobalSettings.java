package com.lorenz.esignagep32.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing global application settings for ESignageP32.
 *
 * Persisted as a singleton row in the "global_settings" table.
 */
@Entity
@Table(name = "global_settings")
@Getter
@Setter
@NoArgsConstructor
public class GlobalSettings {

    /**
     * Singleton primary key (always 1) for global settings.
     */
    @Id
    private Long id = 1L;

    /**
     * Default WiFi SSID for device network connection.
     */
    @Column(nullable = false)
    private String wifiSsid;

    /**
     * Default WiFi password for device network connection.
     */
    @Column(nullable = false)
    private String wifiPassword;

    /**
     * NTP server hostname or IP for clock synchronization.
     */
    @Column(nullable = false)
    private String ntpServer;

    /**
     * Domain of the application server for device communication.
     */
    @Column(nullable = false)
    private String serverDomain;
}
