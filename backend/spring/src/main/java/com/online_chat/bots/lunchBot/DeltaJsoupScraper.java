package com.online_chat.bots.lunchBot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DeltaJsoupScraper {

    public static List<String> lunchOffers() throws IOException {
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

                    offersList.add(description + " — " + price);
                }
            }
        }

        return offersList;
    }

    /*
    public static void main(String[] args) throws IOException {
        DeltaJsoupScraper scraper = new DeltaJsoupScraper();
        System.out.println(scraper.lunchOffers());
    }

     */
}
