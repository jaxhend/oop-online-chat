package com.online_chat.bots.weatherBot;


public class WeatherInfo {
    private final String temperature;
    private final String iconUrl;
    private final String precipitation;
    private final String feelsLike;

    public WeatherInfo(String temperature, String iconUrl, String precipitation, String feelsLike) {
        this.temperature = temperature;
        this.iconUrl = iconUrl;
        this.precipitation = precipitation;
        this.feelsLike = feelsLike;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public String getFeelsLike() {
        return feelsLike;
    }


    public String getTemperature() {
        return temperature;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
