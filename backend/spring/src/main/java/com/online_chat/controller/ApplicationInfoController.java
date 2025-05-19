package com.online_chat.controller;


import com.online_chat.bots.lunchBot.DailyOffer;
import com.online_chat.bots.lunchBot.DeltaJsoupScraper;
import com.online_chat.bots.newsBot.NewsItem;
import com.online_chat.bots.newsBot.RssScraper;
import com.online_chat.bots.weatherBot.WeatherApi;
import com.online_chat.bots.weatherBot.WeatherInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
public class ApplicationInfoController {
    private final RssScraper rssScraper;
    private final WeatherApi weatherAPI;
    private final DeltaJsoupScraper deltaJsoupScraper;

    public ApplicationInfoController(RssScraper rssScraper, WeatherApi weatherAPI, DeltaJsoupScraper deltaJsoupScraper) {
        this.rssScraper = rssScraper;
        this.weatherAPI = weatherAPI;
        this.deltaJsoupScraper = deltaJsoupScraper;
    }

    @GetMapping("/paevapakkumised")
    public ResponseEntity<Map<String, List<DailyOffer>>> getDeals() {
        Map<String, List<DailyOffer>> deals = deltaJsoupScraper.getLatestLunchOffers();
        return ResponseEntity.ok(deals);
    }

    @GetMapping("/ilm")
    public ResponseEntity<WeatherInfo> getWeather() {
        WeatherInfo weatherData = weatherAPI.getLatestWeather();
        return ResponseEntity.ok(weatherData);

    }

    @GetMapping("/uudised")
    public ResponseEntity<List<NewsItem>> getNews(@RequestParam(defaultValue = "1") String topic) {
        List<NewsItem> newsItems = rssScraper.getLatestNews(topic);
        return ResponseEntity.ok(newsItems);
    }

}
