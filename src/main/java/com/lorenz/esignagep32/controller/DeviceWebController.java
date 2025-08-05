package com.lorenz.esignagep32.controller;

import com.lorenz.esignagep32.dto.MessageDto;
import com.lorenz.esignagep32.dto.SetupDto;
import com.lorenz.esignagep32.model.Device;
import com.lorenz.esignagep32.service.DeviceService;
import com.lorenz.esignagep32.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling web requests related to device management.
 */
@Controller
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceWebController {

    private final DeviceService deviceService;            // Service for device CRUD operations
    private final RegistrationService registrationService; // Service to generate device setup configurations

    /**
     * Displays a list of devices for the authenticated user.
     *
     * @param model Spring Model to pass attributes to the view
     * @param user  Authenticated user's details
     * @return view name for device list page
     */
    @GetMapping
    public String listDevices(Model model,
                              @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("devices",
                deviceService.listDevices(user.getUsername()));
        return "devices/list";
    }

    /**
     * Shows details and messages for a specific device.
     *
     * @param id    ID of the device to view
     * @param model Spring Model to pass attributes to the view
     * @return view name for device detail page
     */
    @GetMapping("/{id}")
    public String deviceDetail(@PathVariable Long id, Model model) {
        Device device = deviceService.findById(id);
        model.addAttribute("device", device);
        model.addAttribute("newMessage", new MessageDto()); // Prepare empty message form
        return "devices/detail";
    }

    /**
     * Updates device settings such as name and display intervals.
     *
     * @param id                    ID of the device to update
     * @param name                  New name of the device
     * @param updateIntervalSeconds Interval in seconds between data updates
     * @param rotateIntervalSeconds Interval in seconds between message rotations
     * @return redirect to the device detail page
     */
    @PostMapping("/{id}/settings")
    public String updateSettings(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam int updateIntervalSeconds,
                                 @RequestParam int rotateIntervalSeconds) {
        deviceService.updateSettings(id, name, updateIntervalSeconds, rotateIntervalSeconds);
        return "redirect:/devices/{id}";
    }

    /**
     * Adds a new message to be displayed on the device.
     *
     * @param id  ID of the device
     * @param msg Message DTO containing text, font size, and scroll flag
     * @return redirect to the device detail page
     */
    @PostMapping("/{id}/messages")
    public String addMessage(@PathVariable Long id,
                             @ModelAttribute("newMessage") MessageDto msg) {
        deviceService.addMessage(id,
                msg.getText(),
                msg.getFontSize(),
                msg.isScroll());
        return "redirect:/devices/{id}";
    }

    /**
     * Deletes a specific message from the device.
     *
     * @param deviceId  ID of the device
     * @param messageId ID of the message to delete
     * @return redirect to the device detail page
     */
    @PostMapping("/{deviceId}/messages/{messageId}/delete")
    public String deleteMessage(@PathVariable Long deviceId,
                                @PathVariable Long messageId) {
        deviceService.removeMessage(messageId);
        return "redirect:/devices/{deviceId}";
    }

    /**
     * Deletes the specified device.
     *
     * @param id ID of the device to delete
     * @return redirect to the device list page
     */
    @PostMapping("/{id}/delete")
    public String deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return "redirect:/devices";
    }

    /**
     * Generates and downloads the initial configuration JSON for a device.
     *
     * @param user Authenticated user's details
     * @return ResponseEntity containing the setup JSON as an attachment
     */
    @GetMapping("/init-config")
    public ResponseEntity<SetupDto> downloadInitConfig(
            @AuthenticationPrincipal UserDetails user) {

        SetupDto dto = registrationService.generateSetupConfig(user.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"esignagep32-setup.json\"")
                .body(dto);
    }
}
