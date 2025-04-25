package Bots.WeatherBot;

import java.io.IOException;

public class WeatherData {

    public static void main(String[] args) {
        SeleniumWeatherScraper scraper = new SeleniumWeatherScraper();
        try {
            String temp = scraper.fetchLocationTemp();
            System.out.println(temp);
        } catch (IOException e) {
            System.err.println("Viga temperatuuri pärimisel: " + e.getMessage());
        }
    }
}
