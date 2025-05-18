package com.online_chat.bots.lunchBot;

public class DailyOffer {
    private final String restaurant;
    private final String offer;

    public DailyOffer(String restaurant, String offer) {
        this.restaurant = restaurant;
        this.offer = offer;
    }

    public String getOffer() {
        return offer;
    }

    public String getRestaurant() {
        return restaurant;
    }
}
