package com.lorenz.esignagep32.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

/**
 * Entity representing a registration token issued to a user for provisioning
 * an ESignageP32 device. Tracks usage status and creation timestamp.
 */
@Entity
@Table(name = "registration_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RegistrationToken {

    /**
     * Primary key identifier for the token record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique token string used for device registration.
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * The user who owns this registration token.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Timestamp when the token was created.
     */
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Flag indicating whether the token has been used for registration.
     */
    @Column(nullable = false)
    private boolean used = false;
}
