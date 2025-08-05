package com.lorenz.esignagep32.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing an individual message to display on an ESignageP32 device.
 * <p>
 * Contains text content, rendering options, and associated device relationship.
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
public class DisplayMessage {

    /**
     * Primary key identifier for the display message.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The device this message belongs to.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id")
    private Device device;

    /**
     * The textual content to display (max length 512 characters).
     */
    @Column(nullable = false, length = 512)
    private String text;

    /**
     * The font size multiplier for rendering the message.
     */
    @Column(nullable = false)
    private int fontSize = 1;

    /**
     * Indicates whether the message should scroll on the screen.
     */
    @Column(nullable = false)
    private boolean scroll = false;
}
