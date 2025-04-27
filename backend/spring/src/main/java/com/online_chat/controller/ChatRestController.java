package com.online_chat.controller;


import Bots.LunchBot.DeltaSeleniumScraper;
import Bots.NewsBot.NewsItem;
import Bots.NewsBot.RssScraper;
import Bots.WeatherBot.SeleniumWeatherScraper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        newsItems.forEach(newsItem -> System.out.println(newsItem));
        return ResponseEntity.ok(newsItems);
    }

    @PostMapping("/chatbot")
    public ResponseEntity<String> chatbot(@RequestBody String userMessage) {
        String botResponse = restTemplate.postForObject(
                "http://localhost:5000/flask/chat",
                userMessage,
                String.class
        );
        return ResponseEntity.ok(botResponse);
    }

}
