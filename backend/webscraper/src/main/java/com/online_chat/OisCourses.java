package com.online_chat;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.bson.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

public class OisCourses {
    public static void main(String[] args) {
        Set<String> allCourses = getAllCourses();

        for (String url : allCourses) {
            try {
                getCourseInfo(url);
            } catch (Exception e) {
                System.err.println("Viga kursuse töötlemisel: " + url);
            }
        }

    }
    public static Set<String> getAllCourses() {
        String url = "https://ois2.ut.ee/#/courses";
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get(url);
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
            while (true) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(300);
                long newHeight = (long) js.executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) {
                    break;
                }
                lastHeight = newHeight;
            }
            Set<String> links = new HashSet<>();
            for (WebElement a : driver.findElements(By.tagName("a"))) {
                try {
                    String href = a.getAttribute("href");
                    if (href != null && href.startsWith("https://ois2.ut.ee/#/courses/")) {
                        links.add(href.trim());
                    }
                } catch (StaleElementReferenceException ignored) {
                }
            }
            return links;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }

    }
    public static void getCourseInfo(String baseUrl) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            driver.get(baseUrl);


            WebElement h1Element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            String title = getTitle(h1Element.getText());


            openAllPanels(wait);
            filterData(driver, wait);

        } catch (TimeoutException e) {
            System.err.println("Timeout: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static void filterData(WebDriver driver, WebDriverWait wait) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("webscraping");
        MongoCollection<Document> countersCollection = database.getCollection("counters");
        MongoCollection<Document> collection = database.getCollection("testing");

        WebElement h1Element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String title = getTitle(h1Element.getText());
        String pageText = driver.findElement(By.tagName("body")).getText();

        Map<String, Object> chunk = new HashMap<>();
        chunk.put("_id", getNextId(countersCollection));
        chunk.put("õppeaine_nimi", title);
        addData(pageText, chunk);

        collection.insertOne(new Document(chunk));
        mongoClient.close();
    }

    private static AtomicInteger getNextId(MongoCollection<Document> countersCollection) {
        Document counter = countersCollection.findOneAndUpdate(
                eq("_id", "testing"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().returnDocument(AFTER)
        );
        return new AtomicInteger(counter.getInteger("seq"));
    }


    private static void openAllPanels(WebDriverWait wait) {
        By openAllButton = By.xpath("//button[.//span[normalize-space(.)='Ava kõik']]");
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(openAllButton));
            button.click();
        } catch (NoSuchElementException | TimeoutException e) {
        }
    }


    private static String getTitle(String text) {
        Pattern pattern = Pattern.compile("^(.*?)\\s*\\(\\d+\\s*EAP\\)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
            return matcher.group(1).trim();
        return text.trim();
    }


    private static void addData(String text, Map<String, Object> chunk) {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile("Õppeaine liik\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("õppeaine_liik", clean(matcher.group(1)));

        pattern = Pattern.compile("Kestus semestrites\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("kestus_semestrites", clean(matcher.group(1)));

        pattern = Pattern.compile("Struktuuriüksus\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("struktuuriüksus", clean(matcher.group(1)));

        pattern = Pattern.compile("Lõpphindamise skaala\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("lõpphindamise_skaala", clean(matcher.group(1)));

        pattern = Pattern.compile("Õppekeeled\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("õppekeeled", clean(matcher.group(1)));

        pattern = Pattern.compile("Õppetöö vorm\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("õppetoo_vorm", clean(matcher.group(1)));

        pattern = Pattern.compile("Õppeastmed\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("õppeastmed", clean(matcher.group(1)));

        pattern = Pattern.compile("Eesmärgid\\s+([\\s\\S]*?)(?=\\nÕpiväljundid)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("eesmärgid", clean(matcher.group(1)));


        pattern = Pattern.compile("Õpiväljundid\\s+([\\s\\S]*?)(?=\\nSisu lühikirjeldus)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("õpivaljundid", clean(matcher.group(1)));


        pattern = Pattern.compile("Sisu lühikirjeldus\\s+([\\s\\S]*?)(?=\\n|\\Z)");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("sisu_luhikirjeldus", clean(matcher.group(1)));

        pattern = Pattern.compile("Registreerumise algus\\s+([0-9]{2}\\.[0-9]{2}\\.[0-9]{4})");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("registreerumise_algus", matcher.group(1));

        pattern = Pattern.compile("Registreerumise lõpp\\s+([0-9]{2}\\.[0-9]{2}\\.[0-9]{4})");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("registreerumise_lopp", matcher.group(1));

        pattern = Pattern.compile("Maht\\s+(\\d+)\\s*EAP");
        matcher = pattern.matcher(text);
        if (matcher.find()) chunk.put("maht", matcher.group(1));
    }

    private static String clean(String input) {
        if (input == null) return "";
        return input
                .replaceAll(" +", " ")
                .replaceAll("\\+ ?", "")
                .replaceAll("\\n{2,}", "\n")
                .replaceAll("(?m)^\\s*•\\s*", "- ")
                .replaceAll("^\\s+|\\s+$", "")
                .trim();
    }
    /*

  _id: 42,
  'õppeaine_nimi': 'Keskkond ja mõõtmine',
  'lõpphindamise_skaala': 'Eristav (A, B, C, D, E, F, mi)',
  'õppetoo_vorm': 'Päevaõpe',
  kestus_semestrites: '1',
  'õppeaine_liik': 'Tavaline aine',
  'struktuuriüksus': 'Kolloid- ja keskkonnakeemia õppetool (LTKT04)',
  maht: '3',
  'õppekeeled': 'Inglise keel',
  'õppeastmed': 'Magistriõpe',
  'õpivaljundid': 'Pärast kursust õpilane:\n' +
    '- analüüsib hapniku rolli looduslikes ja tehnoloogilistes protsessides orgaanilise aine biolagundamisel\n' +
    '- hindab toitainete tähtsust veekeemias\n' +
    '- analüüsib laboris biolagunduvuse parameetreid BHT ja KHT\n' +
    '- teab keskkonnaproovide eripära\n' +
    '- arvutab kontsentratsioone ja lahjendusi, koostab proovide keemiliseks analüüsiks kalibreerimisgraafikud.\n' +
    'Lisaks üliõpilane osaleb diskussioonides, väljendab end erialaselt suuliselt ja kirjalikult, mõtleb loovalt, harjutab koostööd ja ajaplaneerimist ning käelisi oskusi.',
  sisu_luhikirjeldus: 'Kursuses antakse ülevaade keskkonna objektide iseloomustamisel kasutatavatest analüüsi meetoditest. Praktiliste tööde käigus teostatakse analüüsid keskkonna objektidelt toodud proovidega ning saadud tulemuste põhjal esitatakse uuritava objekti keskkonnaseisundi iseloomustus koos teostatud mõõtmiste mõõtemääramatuse hinnanguga.',
  'eesmärgid': 'Keskkonnakeemia ja -analüüsi-alaste teadmiste ja oskuste täiendamine.'


     */
}
