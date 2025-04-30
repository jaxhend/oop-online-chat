package com.online_chat.bots.lunchBot;

import java.io.IOException;
import java.util.List;

public class DailyOffer {
    public static void main(String[] args) throws IOException {
        DeltaJsoupScraper scraper = new DeltaJsoupScraper();
        List<String> offers = scraper.lunchOffers();
        System.out.println(offers);
    }
}
