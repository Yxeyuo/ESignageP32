package com.lorenz.esignagep32.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a physical ESignageP32 device assigned to a user.
 * <p>
 * Stores device-specific settings and associated display messages.
 */
@Entity
@Getter
@Setter
@Table(name = "devices")
public class Device {

    /**
     * Primary key identifier for the device.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User-defined name for the device.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Owner of this device; references the User entity.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User owner;

    /**
     * Interval in seconds between server update fetches.
     */
    private int updateIntervalSeconds;

    /**
     * Interval in seconds for rotating through display messages.
     */
    private int rotateIntervalSeconds;

    /**
     * Unique token used by the device to authenticate configuration requests.
     */
    @Column(nullable = false, unique = true)
    private String deviceToken;

    /**
     * List of display messages associated with this device.
     * <p>
     * Cascade operations ensure messages are persisted/removed
     * along with the device.
     */
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DisplayMessage> messages = new ArrayList<>();
}
