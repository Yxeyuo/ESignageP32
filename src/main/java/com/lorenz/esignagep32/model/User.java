package com.lorenz.esignagep32.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing an application user in ESignageP32.
 * <p>
 * Stores credentials and role information for authentication and authorization.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    /**
     * Primary key identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username used for login.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Hashed password for secure authentication.
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * Role assigned to the user (e.g., ROLE_USER, ROLE_ADMIN).
     * Defaults to regular user role.
     */
    @Column(nullable = false)
    private String role = "ROLE_USER";
}
