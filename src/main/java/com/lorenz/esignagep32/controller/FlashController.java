package com.lorenz.esignagep32.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to serve the flash page for ESignageP32 devices.
 */
@Controller
public class FlashController {

    /**
     * Handles GET requests to "/flash" by returning the flash view.
     *
     * @return the name of the flash template to render
     */
    @GetMapping("/flash")
    public String flashPage() {
        return "flash";
    }
}
