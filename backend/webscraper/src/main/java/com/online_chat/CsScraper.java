package com.online_chat;

import com.mongodb.client.*;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.bson.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

public class CsScraper {

    public static void main(String[] args) throws Exception {
        String sitemapUrl = "https://cs.ut.ee/sitemap.xml";

        List<String> etLinks = getEstonianLinks(sitemapUrl);
        System.out.println("Leitud " + etLinks.size() + " linki");

        for (String url : etLinks) {
            scrapeAndSave(url);
        }
    }

    public static List<String> getEstonianLinks(String sitemapUrlStr) throws Exception {
        List<String> etLinks = new ArrayList<>();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new URL(sitemapUrlStr).openStream());
        NodeList urlNodes = doc.getElementsByTagName("url");

        for (int i = 0; i < urlNodes.getLength(); i++) {
            Element urlEl = (Element) urlNodes.item(i);
            NodeList altLinks = urlEl.getElementsByTagName("xhtml:link");
            for (int j = 0; j < altLinks.getLength(); j++) {
                Element alt = (Element) altLinks.item(j);
                if ("et".equals(alt.getAttribute("hreflang"))) {
                    String href = alt.getAttribute("href").replace("⁦", "").replace("⁩", "");
                    etLinks.add(href);
                }
            }
        }
        return etLinks;
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
            Thread.sleep(500);

            // Eemaldab küpsisebänneri kui see takistab
            try {
                ((JavascriptExecutor) driver).executeScript("""
                    const banner = document.querySelector('.eu-cookie-compliance-banner');
                    if (banner) banner.remove();
                """);
                Thread.sleep(100);
            } catch (Exception ignored) {}

            try {
                WebElement cookieButton = driver.findElement(By.id("cookies-button"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cookieButton);
            } catch (NoSuchElementException | ElementClickInterceptedException ignored) {}

            ((JavascriptExecutor) driver).executeScript("""
                ['script','style','noscript'].forEach(tag => {
                    document.querySelectorAll(tag).forEach(e => e.remove());
                });
            """);


            List<By> fallbackSelectors = List.of(
                    By.tagName("main"),
                    By.cssSelector("div.container-narrow"),
                    By.cssSelector("div.container"),
                    By.tagName("body")
            );

            WebElement content = null;
            for (By selector : fallbackSelectors) {
                try {
                    content = driver.findElement(selector);
                    if (!content.getText().isBlank()) break;
                } catch (NoSuchElementException ignored) {}
            }

            if (content == null || content.getText().isBlank()) {
                System.err.println("Ei leidnud sobivat sisu: " + url);
                return;
            }

            String visibleText = content.getText().trim();
            List<String> noiseIndicators = List.of(
                    "TÜ pealehele", "Kiirlingid", "Kõik TÜ üksused", "Otsing", "Est", "Eng", "To front page"
            );
            boolean isOnlyNoise = noiseIndicators.stream().allMatch(visibleText::contains);
            if (isOnlyNoise || visibleText.length() < 100) {
                System.err.println("Vähekasulik leht: " + url);
                return;
            }
            Document doc = new Document()
                    .append("_id", getCounter(counters).getAndIncrement())
                    .append("url", url)
                    .append("sisu", visibleText);

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
                eq("_id", "testing"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(AFTER)
        );
        return new AtomicInteger(counter.getInteger("seq"));
    }
}