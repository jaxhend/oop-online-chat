package com.online_chat.bots.newsBot;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class RssScraper {

    private static final String eestiUudised = "https://www.postimees.ee/rss?r=122";
    private static final String valisUudised = "https://www.postimees.ee/rss?r=123";
    private static final String spordiUudised = "http://sport.postimees.ee/rss/";
    private static final String teadusUudised = "http://www.postimees.ee/rss/?r=145";
    private static final String kultuuriUudised = "http://www.postimees.ee/rss/?r=129";
    private static final Map<String, List<NewsItem>> newsCache = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RssScraper.class);


    private String getFeedUrlByChoice(String chosenTopic) {
        return switch (chosenTopic) {
            case "1" -> eestiUudised;
            case "2" -> valisUudised;
            case "3" -> spordiUudised;
            case "4" -> teadusUudised;
            case "5" -> kultuuriUudised;
            default -> eestiUudised;
        };
    }

    @PostConstruct
    @Scheduled(fixedRate = 1800000)
    public void updateAllFeeds() {
        try {
            logger.info("Uudiste scrapemine algas.");
            for (String topic : List.of("1", "2", "3", "4", "5")) {
                List<NewsItem> items = scrape(topic);
                newsCache.put(topic, items);
            }
            logger.info("Uudiste scrapemine lõppes.");
        } catch (IOException e) {
            logger.error("Uudiste scraperi error", e);
        }
    }

    public List<NewsItem> getLatestNews(String topic) {
        return newsCache.getOrDefault(topic, List.of(
                new NewsItem("Uudiseid pole hetkel saadaval.", "", "", "")
        ));
    }

    private List<NewsItem> scrape(String topic) throws IOException {
        List<NewsItem> newsItems = new ArrayList<>();
        String url = getFeedUrlByChoice(topic);

        Document doc = Jsoup.connect(url)
                .timeout(5000)
                .userAgent("Mozilla")
                .parser(Parser.xmlParser())
                .get();
        Elements items = doc.select("item");

        for (Element item : items) {
            String title = item.selectFirst("title").text();
            String link = item.selectFirst("link").text();
            String publishDateRaw = item.selectFirst("pubDate").text();
            String description = item.selectFirst("description") != null ? item.selectFirst("description").text() : "";

            ZonedDateTime publishDate = ZonedDateTime.parse(publishDateRaw, DateTimeFormatter.RFC_1123_DATE_TIME);
            String formattedPublishDate = publishDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.ENGLISH));

            newsItems.add(new NewsItem(title, link, formattedPublishDate, description));
        }

        return newsItems;
    }
}