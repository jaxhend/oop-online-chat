package com.online_chat.controller;


import com.online_chat.bots.lunchBot.DailyOffer;
import com.online_chat.bots.lunchBot.DeltaJsoupScraper;
import com.online_chat.bots.newsBot.NewsItem;
import com.online_chat.bots.newsBot.RssScraper;
import com.online_chat.bots.weatherBot.WeatherAPI;
import com.online_chat.bots.weatherBot.WeatherInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class InfoController {
    private final RssScraper rssScraper;
    private final WeatherAPI weatherAPI;
    private final DeltaJsoupScraper deltaJsoupScraper;

    public InfoController(RssScraper rssScraper, WeatherAPI weatherAPI, DeltaJsoupScraper deltaJsoupScraper) {
        this.rssScraper = rssScraper;
        this.weatherAPI = weatherAPI;
        this.deltaJsoupScraper = deltaJsoupScraper;
    }

    //@CrossOrigin(origins = "https://www.utchat.ee")
    @CrossOrigin(origins = "*")
    @GetMapping("/paevapakkumised")
    public ResponseEntity<List<DailyOffer>> getDeals() {
        List<DailyOffer> deals = deltaJsoupScraper.getLatestLunchOffers();
        return ResponseEntity.ok(deals);
    }

    //@CrossOrigin(origins = "https://www.utchat.ee")
    @CrossOrigin(origins = "*")
    @GetMapping("/ilm")
    public ResponseEntity<WeatherInfo> getWeather() {
        WeatherInfo weatherData = weatherAPI.getLatestWeather();
        return ResponseEntity.ok(weatherData);

    }

    //@CrossOrigin(origins = "https://www.utchat.ee")
    @CrossOrigin(origins = "*")
    @GetMapping("/uudised")
    public ResponseEntity<List<NewsItem>> getNews(@RequestParam(defaultValue = "1") String topic) {
        List<NewsItem> newsItems = rssScraper.getLatestNews(topic);
        return ResponseEntity.ok(newsItems);
    }

}
