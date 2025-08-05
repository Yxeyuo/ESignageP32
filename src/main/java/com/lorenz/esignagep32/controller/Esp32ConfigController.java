package com.lorenz.esignagep32.controller;

import com.lorenz.esignagep32.dto.ConfigDto;
import com.lorenz.esignagep32.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for serving device configuration to ESignageP32 devices.
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class Esp32ConfigController {

    private final DeviceService deviceService;

    /**
     * Provides the configuration JSON for a specific device if the provided
     * device token matches. Returns 401 Unauthorized otherwise.
     *
     * @param deviceId ID of the device requesting its configuration
     * @param token    Device token from the X-Device-Token header for validation
     * @return ResponseEntity containing the configuration JSON as an attachment
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<ConfigDto> downloadConfig(
            @PathVariable Long deviceId,
            @RequestHeader("X-Device-Token") String token) {

        ConfigDto dto = deviceService.getConfigDto(deviceId);
        if (!dto.getDeviceToken().equals(token)) {
            // Unauthorized if token does not match
            return ResponseEntity.status(401).build();
        }

        // Return configuration JSON as downloadable attachment
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"esignagep32-config.json\"")
                .body(dto);
    }
}
