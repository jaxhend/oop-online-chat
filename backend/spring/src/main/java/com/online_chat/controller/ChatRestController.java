package com.online_chat.controller;


import com.online_chat.bots.lunchBot.DeltaJsoupScraper;
import com.online_chat.bots.newsBot.NewsItem;
import com.online_chat.bots.newsBot.RssScraper;
import com.online_chat.bots.weatherBot.WeatherAPI;
import com.online_chat.bots.weatherBot.WeatherInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
public class ChatRestController {
    private final RssScraper rssScraper;
    private final WeatherAPI weatherAPI;
    private final DeltaJsoupScraper deltaJsoupScraper;

    public ChatRestController(RssScraper rssScraper, WeatherAPI weatherAPI, DeltaJsoupScraper deltaJsoupScraper) {
        this.rssScraper = rssScraper;
        this.weatherAPI = weatherAPI;
        this.deltaJsoupScraper = deltaJsoupScraper;
    }

    @CrossOrigin(origins = {"https://utchat.ee", "https://www.utchat.ee"})
    @GetMapping("/paevapakkumised")
    public ResponseEntity<List<String>> getDeals() throws IOException {
        List<String> deals = deltaJsoupScraper.getLatestLunchOffers();
        return ResponseEntity.ok(deals);
    }

    @CrossOrigin(origins = {"https://utchat.ee", "https://www.utchat.ee"})
    @GetMapping("/ilm")
    public ResponseEntity<WeatherInfo> getWeather() {
        WeatherInfo weatherData = weatherAPI.getLatestWeather();
        return ResponseEntity.ok(weatherData);

    }

    @CrossOrigin(origins = {"https://utchat.ee", "https://www.utchat.ee"})
    @GetMapping("/uudised")
    public ResponseEntity<List<NewsItem>> getNews(@RequestParam(defaultValue = "1") String topic) throws IOException {
        List<NewsItem> newsItems = rssScraper.getLatestNews(topic);
        return ResponseEntity.ok(newsItems);
    }

}
