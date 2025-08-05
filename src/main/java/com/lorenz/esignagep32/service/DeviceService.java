package com.lorenz.esignagep32.service;

import com.lorenz.esignagep32.dto.ConfigDto;
import com.lorenz.esignagep32.dto.MessageDto;
import com.lorenz.esignagep32.model.Device;
import com.lorenz.esignagep32.model.DisplayMessage;
import com.lorenz.esignagep32.model.User;
import com.lorenz.esignagep32.repository.DeviceRepository;
import com.lorenz.esignagep32.repository.DisplayMessageRepository;
import com.lorenz.esignagep32.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing devices, display messages,
 * and providing configuration data for ESignageP32 clients.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepo;
    private final DisplayMessageRepository msgRepo;
    private final UserRepository userRepo;

    @Value("${esp32.wifi.ssid}")
    private String globalSsid;

    @Value("${esp32.wifi.password}")
    private String globalPassword;

    @Value("${esp32.ntp.server}")
    private String ntpServer;

    @Value("${server.domain}")
    private String serverDomain;

    /**
     * Creates a new device for the given user with default settings.
     *
     * @param username   the owner's username
     * @param deviceName the name to assign to the new device
     * @return the created Device entity
     */
    public Device createDevice(String username, String deviceName) {
        User owner = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + username));

        Device device = new Device();
        device.setName(deviceName);
        device.setOwner(owner);
        device.setUpdateIntervalSeconds(60);
        device.setRotateIntervalSeconds(10);
        device.setDeviceToken(UUID.randomUUID().toString());

        return deviceRepo.save(device);
    }

    /**
     * Retrieves all devices owned by the specified user.
     *
     * @param username the owner's username
     * @return list of Device entities
     */
    @Transactional(readOnly = true)
    public List<Device> listDevices(String username) {
        return deviceRepo.findByOwnerUsername(username);
    }

    /**
     * Finds a device by its ID, throwing if not found.
     *
     * @param deviceId the ID of the device to retrieve
     * @return the Device entity
     */
    @Transactional(readOnly = true)
    public Device findById(Long deviceId) {
        return deviceRepo.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not found: " + deviceId));
    }

    /**
     * Updates device name and timing intervals.
     *
     * @param deviceId              the ID of the device to update
     * @param name                  new device name
     * @param updateIntervalSeconds seconds between server updates
     * @param rotateIntervalSeconds seconds between message rotations
     * @return the updated Device entity
     */
    public Device updateSettings(Long deviceId,
                                 String name,
                                 int updateIntervalSeconds,
                                 int rotateIntervalSeconds) {
        Device device = findById(deviceId);
        device.setName(name);
        device.setUpdateIntervalSeconds(updateIntervalSeconds);
        device.setRotateIntervalSeconds(rotateIntervalSeconds);
        return deviceRepo.save(device);
    }

    /**
     * Deletes the specified device.
     *
     * @param deviceId the ID of the device to delete
     */
    public void deleteDevice(Long deviceId) {
        Device device = findById(deviceId);
        deviceRepo.delete(device);
    }

    /**
     * Adds a new display message to the device.
     *
     * @param deviceId the ID of the target device
     * @param text     message text content
     * @param fontSize font size multiplier
     * @param scroll   scroll behavior flag
     * @return the saved DisplayMessage entity
     */
    public DisplayMessage addMessage(Long deviceId,
                                     String text,
                                     int fontSize,
                                     boolean scroll) {
        Device device = deviceRepo.getReferenceById(deviceId);
        DisplayMessage message = new DisplayMessage();
        message.setDevice(device);
        message.setText(text);
        message.setFontSize(fontSize);
        message.setScroll(scroll);
        return msgRepo.save(message);
    }

    /**
     * Removes a display message by its ID.
     *
     * @param messageId the ID of the message to remove
     */
    public void removeMessage(Long messageId) {
        msgRepo.deleteById(messageId);
    }

    /**
     * Constructs a ConfigDto for the device with current settings and messages.
     *
     * @param deviceId the ID of the device
     * @return ConfigDto containing network parameters, timing, and messages
     */
    @Transactional(readOnly = true)
    public ConfigDto getConfigDto(Long deviceId) {
        Device device = deviceRepo.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Device not found: " + deviceId));

        ConfigDto dto = new ConfigDto();
        dto.setWifiSsid(globalSsid);
        dto.setWifiPassword(globalPassword);
        dto.setNtpServer(ntpServer);
        dto.setServerDomain(serverDomain);
        dto.setDeviceId(device.getId());
        dto.setDeviceToken(device.getDeviceToken());
        dto.setUpdateIntervalSeconds(device.getUpdateIntervalSeconds());
        dto.setRotateIntervalSeconds(device.getRotateIntervalSeconds());

        List<MessageDto> messageDtos = device.getMessages().stream()
                .map(m -> {
                    MessageDto md = new MessageDto();
                    md.setText(m.getText());
                    md.setFontSize(m.getFontSize());
                    md.setScroll(m.isScroll());
                    return md;
                })
                .collect(Collectors.toList());
        dto.setMessages(messageDtos);

        return dto;
    }
}