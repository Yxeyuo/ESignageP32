package com.lorenz.esignagep32.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenz.esignagep32.dto.SetupDto;
import com.lorenz.esignagep32.service.RegistrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

/**
 * Controller to generate and serve the SPIFFS configuration binary
 * for ESignageP32 devices over HTTP.
 */
@RestController
@RequestMapping("/esp-web")
public class EspWebController {

    private final RegistrationService regService;
    private final ObjectMapper mapper;

    public EspWebController(RegistrationService regService,
                            ObjectMapper mapper) {
        this.regService = regService;
        this.mapper = mapper;
    }

    /**
     * Builds a temporary SPIFFS image containing the setup JSON
     * and streams it as a binary attachment.
     *
     * @param resp HTTP response to write the binary payload
     * @param user Authenticated principal requesting the image
     * @throws Exception if file operations or process execution fail
     */
    @GetMapping(value = "/config.bin", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void serveConfigBin(HttpServletResponse resp, Principal user) throws Exception {
        // Create temporary directory for SPIFFS assembly
        Path tmpDir = Files.createTempDirectory("spiffs");
        Path fsDir = Files.createDirectory(tmpDir.resolve("fs"));

        // Write setup JSON into the SPIFFS directory
        SetupDto cfg = regService.generateSetupConfig(user.getName());
        Path jsonFile = fsDir.resolve("config.json");
        Files.write(jsonFile, mapper.writeValueAsBytes(cfg));

        // Generate SPIFFS image using external mkspiffs tool
        Path imageFile = tmpDir.resolve("config.bin");
        new ProcessBuilder(
                "mkspiffs",
                "-c", fsDir.toString(),
                "-b", "4096",
                "-p", "256",
                "-s", "0x160000",
                imageFile.toString()
        )
                .inheritIO()
                .start()
                .waitFor();

        // Stream the generated binary back as an attachment
        resp.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"config.bin\"");
        Files.copy(imageFile, resp.getOutputStream());
        resp.getOutputStream().flush();

        // Clean up temporary files and directories
        FileSystemUtils.deleteRecursively(tmpDir);
    }
}
