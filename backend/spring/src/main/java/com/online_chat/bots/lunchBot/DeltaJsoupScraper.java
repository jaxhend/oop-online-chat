package com.online_chat.bots.lunchBot;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DeltaJsoupScraper {

    private List<String> cachedOffers = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        this.cachedOffers = lunchOffers();
    }
    @Scheduled(cron = "0 0 11 * * MON-FRI")
    public void updateLunchOffers() throws IOException {
        cachedOffers = lunchOffers();
    }

    public List<String> getLatestLunchOffers() {
        return cachedOffers;
    }

    public  List<String> lunchOffers() throws IOException {
        String url = "https://xn--pevapakkumised-5hb.ee/tartu/delta-kohvik";

        Document doc = Jsoup.connect(url).get();
        Elements diners = doc.select("div.meal");

        List<String> offersList = new ArrayList<>();
        for (Element diner : diners) {
            String dinerName = diner.select("h3").text();
            if (dinerName.toLowerCase().contains("delta")) {
                System.out.println("Kohvik: " + dinerName);
                Elements offers = diner.select("div.offer");
                for (Element offer : offers) {
                    String description = offer.ownText();
                    String price = offer.select("strong").text();
                    if (!description.isEmpty()) {
                        offersList.add(description + " — " + price);
                    }
                }
            }
        }

        return offersList;
    }


    public static void main(String[] args) throws IOException {
        DeltaJsoupScraper scraper = new DeltaJsoupScraper();
        System.out.println(scraper.lunchOffers());
    }


}
