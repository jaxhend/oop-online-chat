package com.online_chat.bots.lunchBot;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeltaJsoupScraper {

    private static final Logger logger = LoggerFactory.getLogger(DeltaJsoupScraper.class);
    private Map<String, List<DailyOffer>> cachedOffers = new HashMap<>();
    private static final List<String> diners = List.of("delta", "drinkgeld");
    private static final Map<String, String> cafeAliases = Map.of("delta", "Delta kohvik", "drinkgeld", "Drinkgeld / Ülikooli kohvik");
    @PostConstruct
    @Scheduled(cron = "0 30 10 * * MON-FRI")
    public void updateLunchOffers() {
        try {
            logger.info("Päevapakkumiste scrapemine algas.");
            this.cachedOffers = lunchOffers();
            logger.info("Päevapakkumiste scrapemine lõppes.");
        } catch (IOException e) {
            logger.error("Päevapakkumiste scraperi error ", e);
            this.cachedOffers = Map.of("Delta", List.of(new DailyOffer("Delta", "Pakkumiste laadimine ebaõnnestus")));
        }
    }

    public Map<String, List<DailyOffer>> getLatestLunchOffers() {
        return cachedOffers;
    }

    private Map<String, List<DailyOffer>> lunchOffers() throws IOException {
        String url = "https://xn--pevapakkumised-5hb.ee/tartu/delta-kohvik";
        Document doc = Jsoup.connect(url).get();
        Elements dinerList = doc.select("div.meal");

        Map<String, List<DailyOffer>> offersList = new HashMap<>();

        for (Element diner : dinerList) {
            String dinerName = diner.select("h3").text().toLowerCase();
            System.out.println("Leitud kohvik: " + dinerName);

            for (String cafeName : diners) {
                if (dinerName.contains(cafeName)) {
                    String displayName = cafeAliases.getOrDefault(cafeName, cafeName);

                    Elements offers = diner.select("div.offer");
                    List<DailyOffer> parsedOffers = new ArrayList<>();
                    for (Element offer : offers) {
                        String description = offer.ownText();
                        String price = offer.select("strong").text();
                        if (description.endsWith(".")) {
                            description = description.substring(0, description.length() - 1);
                        }
                        if (!description.isEmpty()) {
                            parsedOffers.add(new DailyOffer(displayName, description + "  " + price));
                        }
                    }
                    if (!parsedOffers.isEmpty()) {
                        offersList.put(displayName, parsedOffers);
                    }

                }
            }
        }
        return offersList;
    }

    public static void main(String[] args) throws IOException {
        DeltaJsoupScraper scraper = new DeltaJsoupScraper();
        Map<String, List<DailyOffer>> offers = scraper.lunchOffers();

        for (Map.Entry<String, List<DailyOffer>> entry : offers.entrySet()) {
            System.out.println("☕ " + entry.getKey());
            for (DailyOffer offer : entry.getValue()) {
                System.out.println("  ➤ " + offer.getOffer());
            }
        }
    }

}
