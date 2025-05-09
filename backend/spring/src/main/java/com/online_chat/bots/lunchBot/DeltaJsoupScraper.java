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
import java.util.List;

@Component
public class DeltaJsoupScraper {

    private static final Logger logger = LoggerFactory.getLogger(DeltaJsoupScraper.class);
    private List<DailyOffer> cachedOffers = List.of(new DailyOffer("Delta", "Pakkumised pole veel saadaval"));

    @PostConstruct
    @Scheduled(cron = "0 0 11 * * MON-FRI")
    public void updateLunchOffers() {
        try {
            this.cachedOffers = lunchOffers();
        } catch (IOException e) {
            logger.error("Päevapakkumiste scraperi error ", e);
            this.cachedOffers = List.of(new DailyOffer("Delta", "Pakkumiste laadimine ebaõnnestus"));
        }
    }

    public List<DailyOffer> getLatestLunchOffers() {
        return cachedOffers;
    }

    private List<DailyOffer> lunchOffers() throws IOException {
        String url = "https://xn--pevapakkumised-5hb.ee/tartu/delta-kohvik";

        Document doc = Jsoup.connect(url).get();
        Elements diners = doc.select("div.meal");

        List<DailyOffer> offersList = new ArrayList<>();
        for (Element diner : diners) {
            String dinerName = diner.select("h3").text();
            if (dinerName.toLowerCase().contains("delta")) {
                Elements offers = diner.select("div.offer");
                for (Element offer : offers) {
                    String description = offer.ownText();
                    String price = offer.select("strong").text();
                    if (!description.isEmpty()) {
                        offersList.add(new DailyOffer(dinerName, description + " - " + price));
                    }
                }
            }
        }
        return offersList;
    }

}
