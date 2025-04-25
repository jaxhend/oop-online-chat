package Messages.Commands;

import Server.ClientSession;

public class NewsCommand implements CommandHandler {
    public static final String NEWS_COMMAND = "/uudised";

    @Override
    public boolean matches(String input) {
        return input.startsWith(NEWS_COMMAND);
    }

    @Override
    public String handle(ClientSession session, String input) {
        return "Test: /uudised command triggered";
        /*
        RssScraper rssScraper = new RssScraper();
        String[] commandParts = command.trim().split("\\s+");
        String topic = (commandParts.length > 1) ? commandParts[1] : "1";
        System.out.println("➡️ NewsCommand triggered with command: " + command);

        List<NewsItem> newsItemList = rssScraper.scrape(topic);
        System.out.println("🗞️ Articles fetched: " + newsItemList.size());
        if (newsItemList.isEmpty()) {
            return "Uudiste laadimine ebaõnnestus või uudiseid ei leitud.";
        }
        StringBuilder response = new StringBuilder("Viimased uudised Postimehest:\n\n");

        int newsCount = Math.min(newsItemList.size(), 5);
        for (int i = 0; i <newsCount; i++) {
            NewsItem newsItem = newsItemList.get(i);
            response.append(newsItem.toString());
        }

        return response.toString();

         */
    }


}
