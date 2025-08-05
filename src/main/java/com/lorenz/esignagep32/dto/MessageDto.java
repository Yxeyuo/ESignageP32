package com.lorenz.esignagep32.dto;

import lombok.Data;

/**
 * Data transfer object representing a display message on an ESignageP32 device.
 *
 * Contains the message text, font size, and scroll behavior.
 */
@Data
public class MessageDto {
    /**
     * The text content of the message to display.
     */
    private String text;

    /**
     * The font size to use when rendering the message.
     */
    private int fontSize;

    /**
     * Whether the message should scroll on the display.
     */
    private boolean scroll;
}
