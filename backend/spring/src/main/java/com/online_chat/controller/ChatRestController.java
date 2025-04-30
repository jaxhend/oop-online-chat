package com.online_chat.controller;


import com.online_chat.bots.lunchBot.DeltaJsoupScraper;
import com.online_chat.bots.newsBot.NewsItem;
import com.online_chat.bots.newsBot.RssScraper;
import com.online_chat.bots.weatherBot.WeatherAPI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
public class ChatRestController {
    private final RssScraper rssScraper;
    private final WeatherAPI weatherAPI;

    public ChatRestController(RssScraper rssScraper, WeatherAPI weatherAPI) {
        this.rssScraper = rssScraper;
        this.weatherAPI = weatherAPI;
    }

    @CrossOrigin(origins = "https://utchat.ee")
    @GetMapping("/paevapakkumised")
    public ResponseEntity<List<String>> getDeals() throws IOException {
        List<String> deals = DeltaJsoupScraper.lunchOffers();
        return ResponseEntity.ok(deals);
    }

    @CrossOrigin(origins = "https://utchat.ee")
    @GetMapping("/ilm")
    public ResponseEntity<String> getWeather() {
        String weatherData = weatherAPI.fetchWeather();
        return ResponseEntity.ok(weatherData);

    }

    @CrossOrigin(origins = "https://utchat.ee")
    @GetMapping("/uudised")
    public ResponseEntity<List<NewsItem>> getNews(@RequestParam(defaultValue = "1") String topic) throws IOException {
        List<NewsItem> newsItems = rssScraper.scrape(topic);
        return ResponseEntity.ok(newsItems);
    }

}
