package com.lorenz.esignagep32.controller;

import com.lorenz.esignagep32.model.GlobalSettings;
import com.lorenz.esignagep32.service.GlobalSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Web controller for displaying and updating global application settings.
 */
@Controller
@RequiredArgsConstructor
public class SettingsWebController {

    private final GlobalSettingsService settingsService;

    /**
     * Displays the settings form with current global settings.
     *
     * @param model Spring Model to pass attributes to the view
     * @return name of the settings view template
     */
    @GetMapping("/settings")
    public String showSettings(Model model) {
        GlobalSettings settings = settingsService.getSettings();
        model.addAttribute("settings", settings);
        return "settings";
    }

    /**
     * Saves the updated global settings and redirects with a success flag.
     *
     * @param settings the GlobalSettings object populated from the form
     * @return redirect URL to the settings page with success indicator
     */
    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute("settings") GlobalSettings settings) {
        settingsService.updateSettings(settings);
        return "redirect:/settings?success";
    }
}
