package com.online_chat.bots.weatherBot;


public class WeatherInfo {
    private final String location;
    private final String temperature;
    private final String iconUrl;

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
