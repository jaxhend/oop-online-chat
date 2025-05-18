package com.online_chat;

import com.mongodb.client.*;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.bson.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

public class Teadus {
    public static void main(String[] args) {
        scrapeAndSave("https://cs.ut.ee/et/teadus");
    }

    public static void scrapeAndSave(String url) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = mongoClient.getDatabase("webscraping");
            MongoCollection<Document> counters = db.getCollection("counters");
            MongoCollection<Document> collection = db.getCollection("testing");

            driver.get(url);
            Thread.sleep(1000);

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("""
                const removeTags = ['script', 'style', 'noscript'];
                removeTags.forEach(tag => {
                    document.querySelectorAll(tag).forEach(e => e.remove());
                });
            """);

            WebElement container = driver.findElement(By.cssSelector("div.container-narrow.col-lg-6.order-1.order-lg-2.ut-text"));
            List<WebElement> elements = container.findElements(By.xpath(".//h3 | .//p"));

            Map<String, StringBuilder> content = new LinkedHashMap<>();
            String currentHeading = null;

            for (WebElement el : elements) {
                String tag = el.getTagName();
                String text = el.getText().trim();
                if (tag.equals("h3")) {
                    currentHeading = text;
                    content.putIfAbsent(currentHeading, new StringBuilder());
                } else if (tag.equals("p") && currentHeading != null) {
                    content.get(currentHeading).append(text).append("\n");
                }
            }

            AtomicInteger id = getCounter(counters);
            Document doc = new Document("_id", id.getAndIncrement()).append("url", url);
            for (Map.Entry<String, StringBuilder> entry : content.entrySet()) {
                doc.append(entry.getKey(), entry.getValue().toString().trim());
            }

            collection.insertOne(doc);
            System.out.println("Salvestatud: " + url);

        } catch (Exception e) {
            System.err.println("Viga: " + url + " – " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static AtomicInteger getCounter(MongoCollection<Document> counters) {
        Document counter = counters.findOneAndUpdate(
                eq("_id", "teadus"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER)
        );
        return new AtomicInteger(counter.getInteger("seq"));
    }
}