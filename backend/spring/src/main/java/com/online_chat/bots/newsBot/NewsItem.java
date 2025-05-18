package com.online_chat.bots.newsBot;

public class NewsItem {
    private final String title;
    private final String link;
    private final String publishDate;
    private final String description;


    public NewsItem(String title, String link, String publishDate, String description) {
        this.title = title;
        this.link = link;
        this.publishDate = publishDate;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public String getDescription() {
        return description;
    }
}

