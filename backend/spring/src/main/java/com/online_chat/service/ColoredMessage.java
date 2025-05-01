package com.online_chat.service;

public class ColoredMessage {
    private String text;
    private String color;

    public ColoredMessage(String text, String color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public String getColor() {
        return color;
    }
}
