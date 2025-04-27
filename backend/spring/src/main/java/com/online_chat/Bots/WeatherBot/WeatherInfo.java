package com.online_chat.Bots.WeatherBot;

import java.io.IOException;



public class WeatherInfo {
    private String location;
    private String temperature;
    private String iconUrl;

    public WeatherInfo(String location, String temperature, String iconUrl) {
        this.location = location;
        this.temperature = temperature;
        this.iconUrl = iconUrl;
    }

    public String getLocation() {
        return location;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
