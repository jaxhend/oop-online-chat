package Bots.NewsBot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class RssScraper {
    private static final String eestiUudised = "https://www.postimees.ee/rss?r=122";
    private static final String välisUudised = "https://www.postimees.ee/rss?r=123";
    private static final String spordiUudised = "http://sport.postimees.ee/rss/";
    private static final String teadusUudised = "http://www.postimees.ee/rss/?r=145";
    private static final String kultuuriUudised = "http://www.postimees.ee/rss/?r=129";


    public String chooseNewsTopic() {
        System.out.println("Vali rubriik ja sisesta vastav number:");
        System.out.println("1 - Eesti uudised\n" +
                "2 - Välisuudised\n" +
                "3 - Spordiuudised\n" +
                "4 - Teadusuudised\n" +
                "5 - Kultuuriuudised");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();

    }

    private String getFeedUrlByChoice(String chosenTopic) {
        return switch (chosenTopic) {
            case "1" -> eestiUudised;
            case "2" -> välisUudised;
            case "3" -> spordiUudised;
            case "4" -> teadusUudised;
            case "5" -> kultuuriUudised;
            default -> eestiUudised;
        };
    }
    public List<NewsItem> scrape (String topic) {
        List<NewsItem> newsItems = new ArrayList<>();
        String url = getFeedUrlByChoice(topic);


        try{
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

        } catch (Exception e) {
            System.out.println("Error while scraping news items: " + e.getMessage());
        }
        return newsItems;
    }




    public static void main(String[] args) {
        RssScraper scraper = new RssScraper();
        List<NewsItem> newsItems = scraper.scrape(scraper.chooseNewsTopic());
        for (NewsItem newsItem : newsItems) {
            System.out.println(newsItem);
        }
    }


}
