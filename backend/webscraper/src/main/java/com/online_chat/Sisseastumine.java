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

public class Sisseastumine {

    public static void main(String[] args) {
        scrapeAdmissionPage("https://cs.ut.ee/et/sisseastumine");
    }

    public static void scrapeAdmissionPage(String url) {
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
                const tags = ['script', 'style', 'noscript', 'svg', 'footer', 'header'];
                tags.forEach(tag => document.querySelectorAll(tag).forEach(el => el.remove()));
            """);

            // Leia sissejuhatav tekst (body -> container)
            WebElement introDiv = driver.findElement(By.cssSelector("div.tab-pane.fade.active.show div.col-12"));
            String introText = introDiv.getText().trim();

            // Tabide sisu kraapimine
            Map<String, String> tabContent = new LinkedHashMap<>();
            String[] tabs = {"Bakalaureuseõpe", "Magistriõpe", "Doktoriõpe"};

            for (String tabName : tabs) {
                try {
                    WebElement tabButton = driver.findElement(By.xpath("//button[normalize-space(text())='" + tabName + "']"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tabButton);
                    Thread.sleep(500);

                    WebElement activePane = driver.findElement(By.cssSelector("div.tab-pane.fade.active.show"));
                    WebElement colContent = activePane.findElement(By.cssSelector("div.col-12"));
                    tabContent.put(tabName, colContent.getText().trim());
                } catch (Exception e) {
                    System.err.println("Ei saanud tab'i: " + tabName);
                    tabContent.put(tabName, "Ei õnnestunud laadida");
                }
            }

            // Salvestus MongoDB-sse
            Document doc = new Document()
                    .append("_id", getNextId(counters).getAndIncrement())
                    .append("url", url)
                    .append("sissejuhatus", introText)
                    .append("õppeinfo", tabContent);

            collection.insertOne(doc);
            System.out.println("Salvestatud: " + url);

        } catch (Exception e) {
            System.err.println("Viga: " + url + " – " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static AtomicInteger getNextId(MongoCollection<Document> counters) {
        Document counter = counters.findOneAndUpdate(
                eq("_id", "csut_sisseastumine"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER)
        );
        return new AtomicInteger(counter.getInteger("seq"));
    }
}