package Webscraper;

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

public class OisCurricula {

    public static void main(String[] args) {
        Set<String> veebilehed = getAllCurriculas();
        for (String s : veebilehed) {
            getCurriculaInfo(s);
        }

//        Testimiseks
//        getCurriculaInfo("https://ois2.ut.ee/#/curricula/2476");
    }


    public static Set<String> getAllCurriculas() {
        String url = "https://ois2.ut.ee/#/curricula";
        WebDriverManager.chromedriver().setup(); // Seab automaatselt WebDriveri üles
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            driver.get(url);
            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
            while (true) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);"); // Kerib veebilehe lõppu.
                Thread.sleep(200); // Ootab, kuni sisu laeb.
                long newHeight = (long) js.executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) {
                    break;
                }
                lastHeight = newHeight;
            }

            // Leiab kõik lingid, mis algavad "https://ois2.ut.ee/#/curricula"
            List<WebElement> anchors = driver.findElements(By.tagName("a"));
            Set<String> links = new HashSet<>();
            for (WebElement a : anchors) {
                String href = a.getAttribute("href");
                if (href != null && !href.trim().isEmpty() && href.startsWith("https://ois2.ut.ee/#/curricula/")) {
                    links.add(href.trim());
                }
            }
            return links; // Tagastab 184 linki

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }


    public static void getCurriculaInfo(String baseUrl) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        try {
            findValidVersion(driver, baseUrl); // Õige veebilehe valimine
            openAllPanels(wait); // Moodulite kirjelduse avamine
            filterData(driver, wait); // MongoDB andmebaasi salvestamine.

        } catch (TimeoutException e) {
            System.err.println("Timeout: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private static void findValidVersion(WebDriver driver, String baseUrl) {
        String[] versions = {"2025", "2024"};
        for (String version : versions) {
            String url = baseUrl + "/version/" + version;
            driver.get(url);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            boolean notFound = !driver.findElements(By.tagName("ois2-study-unit-shared-page-not-found")).isEmpty();
            if (!notFound) {
                return;
            }
        }
    }


    private static void openAllPanels(WebDriverWait wait) {
        By openAllButton = By.xpath("//button[.//span[normalize-space(.)='Ava kõik']]");
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(openAllButton));
            button.click();
        } catch (NoSuchElementException | TimeoutException e) {
            System.err.println("Nuppu ei ole");
        }
    }

    //  Vana kood: List<WebElement> panels = driver.findElements(By.tagName("ois2-study-unit-shared-study-info-panel"));
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
        chunk.put("õppekava", title);
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
        return new AtomicInteger(counter.getInteger("seq")); // ID väärtus
    }

    private static String getTitle(String text) {
        Pattern pattern = Pattern.compile("^[^)]*\\)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
            return matcher.group(0).trim();
        return null;
    }

    /*
    {
      "_id": 189,
      "lopetamise_tingimused": "Peaeriala omandamiseks miinimummahus tuleb üliõpilasel sooritada (108 EAP):\n1.1. Matemaatika ...
      "õppeaste": "Bakalaureuseõpe",
      "programmijuht": "Varmo Vene",
      "õppekava_inglise_keeles": "Computer Science",
      "õppekava": "Informaatika (180 EAP)",
      "nominaalkestus": 3,
      "instituut": "Arvutiteaduse instituut LTAT",
      "sisu": "Õppekava koosneb\nkahest alusmoodulist (kumbki 24 EAP):\n- matemaatika alusmoodul;\n- IT alusmoodul ;\nkolmest ...
      "opivaljundid": "Õppekava läbinud üliõpilane\n1) omab süsteemset ülevaadet arvutiteaduse teoreetilistest printsiipidest, ...
      "õppekeeled": [
        "Eesti",
        "Inglise keel"
      ],
      "kirjeldus": "Bakalaureuse õppekava informaatika erialal (peaerialana) läbimisel omandatakse üldised ja praktilised põhiteadmised ...
      "õppevorm": "Päevaõpe",
      "valdkond": "Loodus- ja täppisteaduste valdkond LT",
      "vastuvõtutingimused": "Kandideerida on õigus isikul, kellel on keskharidus või sellele vastav kvalifikatsioon. Õppekava ...
      "moodulid": "1. Alusmoodulid\n48 EAP\nalusmoodul\n1.1 Matemaatika alusmoodul | kohustuslik\n24 EAP\nValiku põhimõtted ...
    }
     */

    private static void addData(String text, Map<String, Object> chunk) {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile("Õppeaste\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("õppeaste", matcher.group(1));

        pattern = Pattern.compile("Õppekeeled\\s+([^\n]+)\\s+Teised vajalikud keeled\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("õppekeeled", List.of(matcher.group(1), matcher.group(2)));

        pattern = Pattern.compile("Nominaalkestus aastates\\s+(\\d)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("nominaalkestus", Integer.parseInt(matcher.group(1)));

        pattern = Pattern.compile("Õppekohtade koguarv\\s+(\\d+)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("õppekohtade_arv", matcher.group(1));

        pattern = Pattern.compile("Programmijuht\\s*(?:\\S*\\s*){0,2}?([A-ZÕÄÖÜ][a-zõäöü]+\\s+[A-ZÕÄÖÜ][a-zõäöü]+)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("programmijuht", matcher.group(1));

        pattern = Pattern.compile("Õppekava nimetus inglise keeles[\\s\\S]*?\\r?\\n([^\r\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("õppekava_inglise_keeles", matcher.group(1).trim());

        pattern = Pattern.compile("Õppevorm[\\s\\S]*?\\r?\\n([^\r\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("õppevorm", matcher.group(1).trim());

        // Vahepeal jätab vahele rea Detailid ja võtab järgmise rea.
        pattern = Pattern.compile("Valdkond(?:\\nDetailid)?\\n(.*?)([A-Z]+)Koordineerija");
        matcher = pattern.matcher(text); // Loodus- ja täppisteaduste valdkondLT
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            chunk.put("valdkond", value + " " + code);
        }

        // Vaatab rida, millele ei järgne Valdkond ega Detailid.
        pattern = Pattern.compile("Õppekava haldajad\\n(?!Valdkond|Detailid)(.*?)([A-Z]+)Koordineerija");
        matcher = pattern.matcher(text); // Arvutiteaduse instituutLTAT
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            String code = matcher.group(2).trim();
            chunk.put("instituut", value + " " + code);
        }

        pattern = Pattern.compile("(?s)Kirjeldus.*?Üldeesmärgid\\s+(.*?)(?=\\nÕpiväljundid)");
        matcher = pattern.matcher(text);
        if (matcher.find())
            chunk.put("kirjeldus", matcher.group(1).trim());

        pattern = Pattern.compile("(?s)Sisu lühikirjeldus\\s+(.*?)(?:\\n\\n|\\Z|Vastuvõtutingimused)");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            chunk.putIfAbsent("sisu", matcher.group(1).trim());
        }

        pattern = Pattern.compile("(?s)Vastuvõtutingimused\\s+(.*?)(?:\\n\\n|\\Z|Lõpetamise tingimused)");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            chunk.putIfAbsent("vastuvõtutingimused", matcher.group(1).trim());
        }

        pattern = Pattern.compile("(?s)Lõpetamise tingimused\\s+(.*?)(?:\\n\\n|\\Z)");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            chunk.putIfAbsent("lopetamise_tingimused", matcher.group(1).trim());
        }

        pattern = Pattern.compile("(?s)Õpiväljundid\\s+(.*?)(?:\\n\\n|\\Z)");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            chunk.putIfAbsent("opivaljundid", matcher.group(1).trim());
        }

        // Moodulite info lisamine
        pattern = Pattern.compile("(?s)Moodulid\\s*(.*?)(?=\\nÜldinfo|\\Z)");
        matcher = pattern.matcher(text);

        if (matcher.find()) {
            String moodulidContent = matcher.group(1).trim();
            String[] lines = moodulidContent.split("\\R");
            List<String> newLines = new ArrayList<>();
            pattern = Pattern.compile("^\\s*\\d+\\s+EAP\\s*$"); // Ainete ja EAP info väljafiltreerimine

            for (int i = 4; i < lines.length; i++) {
                String currentLine = lines[i];
                if (currentLine.trim().startsWith("●")) {
                    if (i + 1 < lines.length && pattern.matcher(lines[i + 1]).matches())
                        i++;
                } else {
                    newLines.add(currentLine);
                }
            }

            if (!newLines.isEmpty())
                chunk.put("moodulid", String.join("\n", newLines));
        }
    }
}

