package com.online_chat.Bots.NewsBot;

public class NewsItem {
    private String title;
    private String link;
    private String publishDate;
    private String description;


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

    public String getDescription() {
        return description;
    }

    public String getPublishDate() {
        return publishDate;
    }

    @Override
    public String toString() {
        return """
        📰 %s
        📅 %s
        📝 %s
        🔗 %s
        
        """.formatted(title, publishDate, description, link);
    }
}

