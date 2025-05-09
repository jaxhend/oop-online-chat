package com.online_chat.bots.weatherBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Component
public class WeatherAPI {

    @Value("${weather.key}")
    private String API_KEY;
    private static final String ENDPOINT = "https://api.weatherapi.com/v1/current.json?key=%s&q=Tartu,Estonia&lang=et";
    private static final Logger logger = LoggerFactory.getLogger(WeatherAPI.class);
    private WeatherInfo latestWeather;

    @PostConstruct
    @Scheduled(fixedRate = 600000)
    public void scheduledUpdate() {
        try {
            this.latestWeather = fetchWeather();
        } catch (IOException e) {
            logger.error("Ilma API teenuse error", e);
        }
    }

    public WeatherInfo getLatestWeather() {
        return latestWeather;
    }

    private WeatherInfo fetchWeather() throws IOException {
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

        return new WeatherInfo(temp, icon, precip, feelsLike);
    }

}
