package com.online_chat.bots.weatherBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Component
public class WeatherAPI {
private static final String API_KEY = "557863b6445245e084e55902253004";
private static final String ENDPOINT = "https://api.weatherapi.com/v1/current.json?key=%s&q=tartu&lang=et";


    public static String fetchWeather() {
        try{
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

            String temp = root.path("current").path("temp_c").asText() + "°C";
            String icon = "https: " + root.path("current").path("condition").path("icon").asText();
            return String.format("{\"temperatuur\": \"%s\", \"icon\": \"%s\"}", temp, icon);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        WeatherAPI weatherAPI = new WeatherAPI();
        System.out.println(weatherAPI.fetchWeather());
    }
}
