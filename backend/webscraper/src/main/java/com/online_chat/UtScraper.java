package com.online_chat;


import com.mongodb.client.*;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.bson.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

public class UtScraper {

    private static final String SITEMAP_URL = "https://ut.ee/sitemap.xml";
    private static final LocalDate SINCE_DATE = LocalDate.of(2024, 1, 1);

    public static void main(String[] args) throws Exception {
        List<String> urls = extractUrlsFromSitemap(SITEMAP_URL);
        List<String> filtered = urls.stream().filter(u -> u.startsWith("https://ut.ee/et/")).toList();
        for (String url : filtered) {
            scrapeAndSave(url);
        }
    }

    // Parsib xml-st URL-id ja tagastab ainult need, mis on uuendatud viimati vähemalt 2024
    private static List<String> extractUrlsFromSitemap(String url) throws Exception {
        List<String> urls = new ArrayList<>();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new URL(url).openStream());
        doc.getDocumentElement().normalize();

        NodeList urlNodes = doc.getElementsByTagName("url");
        for (int i = 0; i < urlNodes.getLength(); i++) {
            org.w3c.dom.Element el = (org.w3c.dom.Element) urlNodes.item(i);
            String loc = el.getElementsByTagName("loc").item(0).getTextContent().trim();
            NodeList lmList = el.getElementsByTagName("lastmod");
            if (lmList.getLength() > 0) {
                String lastmodStr = lmList.item(0).getTextContent().trim();
                LocalDate lastmod = OffsetDateTime.parse(lastmodStr).toLocalDate();
                if (!lastmod.isBefore(SINCE_DATE)) {
                    urls.add(loc);
                }
            }
        }
        return urls;
    }

    private static void scrapeAndSave(String url) {
        WebDriverManager.chromedriver().setup(); // Seab automaatselt WebDriveri üles
        ChromeOptions options = new ChromeOptions().addArguments("--headless=new", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = mongoClient.getDatabase("webscraping");
            MongoCollection<Document> counterCol = db.getCollection("counters");
            MongoCollection<Document> collection = db.getCollection("testing");

            driver.get(url);
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

            List<Map<String, String>> sections = extractSections(driver);

            for (Map<String, String> sec : sections) {
                Document doc = new Document();
                doc.put("_id", getNextId(counterCol));
                doc.put("url", url);
                doc.put("pealkiri", sec.get("pealkiri"));
                doc.put("sisu", sec.get("sisu"));
                collection.insertOne(doc);
            }
        } catch (Exception e) {
            System.err.println("Error scraping " + url + ": " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static List<Map<String, String>> extractSections(WebDriver driver) {
        List<Map<String, String>> sections = new ArrayList<>();
        List<WebElement> elements = driver.findElements(By.cssSelector("h1, h2, h3, h4, p, div"));
        elements.sort(Comparator.comparingInt(e -> e.getLocation().getY()));

        String heading = null;
        StringBuilder sb = new StringBuilder();

        for (WebElement el : elements) {
            String tag = el.getTagName().toLowerCase();
            String text = el.getText().trim();
            if (text.isEmpty()) continue;

            if (tag.matches("h[1-4]")) {
                if (heading != null && sb.length() > 0) {
                    sections.add(Map.of("pealkiri", heading, "sisu", sb.toString().trim()));
                }
                heading = text;
                sb.setLength(0);
            } else if (heading != null) {
                sb.append(text).append("\n");
            }
        }

        if (heading != null && sb.length() > 0) {
            sections.add(Map.of("pealkiri", heading, "sisu", sb.toString().trim()));
        }

        return sections;
    }

    // Tõstab counter väärtuse ja tagastab uue ID MongoDB jaoks
    private static int getNextId(MongoCollection<Document> counters) {
        Document counter = counters.findOneAndUpdate(
                eq("_id", "testing"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().returnDocument(AFTER)
        );

        if (counter == null || !counter.containsKey("seq") || counter.get("seq") == null) {
            throw new IllegalStateException("Counter dokument 'ut_pages' puudub või väli 'seq' on määramata.");
        }

        return counter.getInteger("seq");
    }
}