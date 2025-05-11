package com.online_chat.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MessageFormatter {
    public static final String GREEN = "#43B581";    // Heleroheline
    public static final String COMMANDS = "#9B59B6";     // Lilla
    public static final String ERRORS = "#E74C3C";  // Punane
    public static final String ORANGE = "#D35400";   // Oranž
    public static final String WET_ASPHALT = "#34495E"; // Hallikas
    @JsonProperty
    private String text;
    @JsonProperty
    private String color;

    public MessageFormatter(String text, String color) {
        this.text = "[" + currentTime() + "] " + text;
        this.color = color;
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

}
