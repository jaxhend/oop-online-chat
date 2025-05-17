package com.online_chat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Embeddable
public class MessageFormatter {
    public static final String BLACK = "#000000"; // Sõnumid
    public static final String GREEN = "#43B581"; // Heleroheline
    public static final String PURPLE = "#9B59B6"; // Lilla
    public static final String RED = "#E74C3C";  // Punane
    public static final String ORANGE = "#D35400";   // Oranž
    public static final String WET_ASPHALT = "#34495E"; // Hallikas
    public static final String BLUE = "#3B82F6"; // Ülikooli sinine

    @JsonProperty
    private String text;
    @JsonProperty
    private String color;

    public MessageFormatter(String text, String color) {
        this.text = "[" + currentTime() + "] " + text;
        this.color = color;
    }

    // JPA jaoks
    public MessageFormatter() {
    }

    public static String currentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
