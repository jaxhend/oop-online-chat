package com.online_chat.bots.lunchBot;

import java.io.IOException;
import java.util.List;

public class DailyOffer {
    private final String restaurant;
    private final String offer;

    public DailyOffer(String restaurant, String offer) {
        this.restaurant = restaurant;
        this.offer = offer;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public String getOffer() {
        return offer;
    }

    public static void main(String[] args) throws IOException {
        DeltaJsoupScraper scraper = new DeltaJsoupScraper();
        List<DailyOffer> offers = scraper.lunchOffers();
        System.out.println(offers);
    }
}
