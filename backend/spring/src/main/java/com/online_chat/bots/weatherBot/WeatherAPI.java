package com.online_chat.bots.weatherBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Component
public class WeatherAPI {

    @Value("${weather.key}")
    private String API_KEY;
    private WeatherInfo latestWeather;
    private static final String ENDPOINT = "https://api.weatherapi.com/v1/current.json?key=%s&q=Tartu,Estonia&lang=et";

    @PostConstruct
    public void init() {
        this.latestWeather = fetchWeather();
    }
    @Scheduled(fixedRate = 1800000)
    public void scheduledUpdate() {
        this.latestWeather = fetchWeather();
    }

    public WeatherInfo getLatestWeather() {
        return latestWeather;
    }

    public WeatherInfo fetchWeather() {
        try {
            String requestUrl = String.format(ENDPOINT, API_KEY);
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder jsonText = new StringBuilder();
            while (scanner.hasNext()) {
                jsonText.append(scanner.nextLine());
            }
            scanner.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonText.toString());

            String temp = root.path("current").path("temp_c").asText() + " °C";
            String feelsLike = root.path("current").path("feelslike_c").asText() + " °C";
            String precip = root.path("current").path("precip_mm").asText() + " mm";
            String icon = "https:" + root.path("current").path("condition").path("icon").asText();

            return new WeatherInfo(temp,icon,precip,feelsLike);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        WeatherAPI weatherAPI = new WeatherAPI();
        System.out.println(weatherAPI.fetchWeather());
    }
}
