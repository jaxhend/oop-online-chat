package com.online_chat.controller;


import com.online_chat.bots.lunchBot.DeltaSeleniumScraper;
import com.online_chat.bots.newsBot.NewsItem;
import com.online_chat.bots.newsBot.RssScraper;
import com.online_chat.bots.weatherBot.SeleniumWeatherScraper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;


@RestController
public class ChatRestController {
    private final RssScraper rssScraper;
    private final SeleniumWeatherScraper seleniumWeatherScraper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final DeltaSeleniumScraper deltaSeleniumScraper;

    public ChatRestController(RssScraper rssScraper, SeleniumWeatherScraper seleniumWeatherScraper, DeltaSeleniumScraper deltaSeleniumScraper) {
        this.rssScraper = rssScraper;
        this.seleniumWeatherScraper = seleniumWeatherScraper;
        this.deltaSeleniumScraper = deltaSeleniumScraper;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/paevapakkumised")
    public ResponseEntity<List<String>> getDeals() {
        List<String> deals = deltaSeleniumScraper.fetchLunchOffer();
        return ResponseEntity.ok(deals);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/ilm")
    public ResponseEntity<String> getWeather() throws IOException {
        String weatherData = seleniumWeatherScraper.fetchWeather();
        return ResponseEntity.ok(weatherData);

    }

    @CrossOrigin(origins = "*")
    @GetMapping("/uudised")
    public ResponseEntity<List<NewsItem>> getNews(@RequestParam(defaultValue = "1") String topic) throws IOException {
        List<NewsItem> newsItems = rssScraper.scrape(topic);
        return ResponseEntity.ok(newsItems);
    }

    @PostMapping("/chatbot")
    public ResponseEntity<String> chatbot(@RequestBody String userMessage) {
        String flaskUrl = "http://localhost:5000/flask/chat";
        String userId = "UserID";
        String prompt = userMessage;
        String jsonPayload = String.format("{\"user_id\": \"%s\", \"prompt\": \"%s\"}", userId, prompt);

        try {
            String botResponse = restTemplate.postForObject(
                    flaskUrl,
                    jsonPayload,
                    String.class
            );
            return ResponseEntity.ok(botResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error communicating with Flask API.");
        }
    }

}
