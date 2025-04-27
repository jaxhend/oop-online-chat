package com.online_chat.bots.lunchBot;

import java.util.List;

public class DailyOffer {
    public static void main(String[] args) {
        DeltaSeleniumScraper scraper = new DeltaSeleniumScraper();
        List<String> offers = scraper.fetchLunchOffer();
        System.out.println(offers);
    }
}
