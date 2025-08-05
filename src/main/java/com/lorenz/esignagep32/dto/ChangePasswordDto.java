// src/main/java/com/lorenz/esignagep32/dto/ChangePasswordDto.java
package com.lorenz.esignagep32.dto;

import lombok.Data;

/**
 * DTO for change-password form inputs.
 */
@Data
public class ChangePasswordDto {
    /** The user's current password for verification. */
    private String currentPassword;
    /** The new password to set. */
    private String newPassword;
    /** Confirmation of the new password. */
    private String confirmPassword;
}
