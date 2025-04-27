package Bots.LunchBot;

public class DailyOffer {
    public static void main(String[] args) {
        DeltaSeleniumScraper scraper = new DeltaSeleniumScraper();
        String offers = scraper.fetchLunchOffer();
        System.out.println(offers);
    }
}
