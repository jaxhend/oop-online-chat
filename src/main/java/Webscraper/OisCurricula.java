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

//        getCurriculaInfo("https://ois2.ut.ee/#/curricula/2476");
    }


    public static Set<String> getAllCurriculas() {
        String url = "https://ois2.ut.ee/#/curricula";
        WebDriverManager.chromedriver().setup(); // Seab automaatselt WebDriveri üles
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu", "--window-size=1920,1080");
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
        options.addArguments("--disable-gpu", "--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        try {
            String workingUrl = findValidVersion(driver, baseUrl);
            if (workingUrl == null) {
                System.err.println("Töötavat veebilehte ei leitud!");
                return;
            }
            openAllPanels(wait, driver); // Moodulite kirjelduse avamine
            saveAllChunks(driver); // MongoDB andmebaasi salvestamine.

        } catch (TimeoutException e) {
            System.err.println("Timeout: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private static String findValidVersion(WebDriver driver, String baseUrl) {
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
                return url;
            }
        }
        return null;
    }


    private static void openAllPanels(WebDriverWait wait, WebDriver driver) {
        By openAllButton = By.xpath("//button[.//span[normalize-space(.)='Ava kõik']]");
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(openAllButton));
            button.click();
        } catch (NoSuchElementException | TimeoutException e) {
            System.out.println(driver.getCurrentUrl());
        }
    }



    private static void saveAllChunks(WebDriver driver) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("webscraping");
        MongoCollection<Document> countersCollection = database.getCollection("counters");
        MongoCollection<Document> collection = database.getCollection("testing");

        WebElement h1Element = driver.findElement(By.tagName("h1"));
        List<WebElement> panels = driver.findElements(By.tagName("ois2-study-unit-shared-study-info-panel"));
        String title = getTitle(h1Element.getText());
        String oppeaste = getLevel(panels.get(0).getText());

        Map<String, Object> chunk1 = new HashMap<>();
        Map<String, Object> chunk2 = new HashMap<>();
        Map<String, Object> chunk3 = new HashMap<>();

        /*
        "chunk_id": 1,
        "õppekava": "Informaatika (180 EAP)",
        "level": "Bakalaureuseõpe", */
        List.of(chunk1, chunk2, chunk3).forEach(elem -> {
            elem.put("_id", getNextId(countersCollection));
            elem.put("õppekava", title);
            elem.put("õppeaste", oppeaste);
        });

        // Käib kõik andmed läbi, et luua terviklik sõnastik.
        for (WebElement panel : panels) {
            String text = panel.getText();
            extractChunk1Data(text, chunk1);
            extractChunk2Data(text, chunk2);
            extractChunk3Data(text, chunk3);
        }
        // Salvesta andmebaasi
        collection.insertMany(List.of(new Document(chunk1), new Document(chunk2)));
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

    private static String getLevel(String text) {
        Pattern pattern = Pattern.compile("Õppeaste\\s+([^\n]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
            return matcher.group(1);
        return null;
    }

    private static String getTitle(String text) {
        Pattern pattern = Pattern.compile("^[^)]*\\)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
            return matcher.group(0);
        return null;
    }


/*
        {
        OK"oppekeeled": ["eesti", "inglise"],
        OK"kestvus_aastates": 3,
        !!!"oppekohtade_arv": null,
        !!!"programmijuht": "Varmo Vene",
        !!!"oppekava_ingliskeelne_nimetus": "Computer Science"
        !!!"õppeliik": "Päevaõpe"
        !!!"oppekava_maht": "180 EAP"
        !!!"valdkond": "Loodus- ja täppisteaduste valdkond LT"
        !!!"instituut": "Arvutiteaduse instituut LTAT"
        !!!"kirjeldus": "Bakalaureuse õppekava informaatika erialal (peaerialana) läbimisel omandatakse üldised ja praktilised
        põhiteadmised arvutiteaduses ning selle mitmetes alamvaldkondades, sh tarkvaraarenduses (analüüs, kavandamine,
            teostamine, testimine). Saadakse vajalikud algteadmised lähemates kõrvaldistsipliinides, nagu pidev ja diskreetne
        matemaatika, matemaatiline statistika ja andmeanalüüs ning arvutisuhtlus, ja ühtlasi tõdemus, et hariduse omandamine
        selles valdkonnas on tõepoolest elukestev protsess. Üldoskustest on õppekava ainetes rõhutatud eeskätt ülesannete
        lahendamise oskust, erialase (nii üld-arvutiteaduse kui ka tarkvaratehnikaalase) kirjandusega töötamist,
        eneseväljendamise oskust nii sõnas kui ka kirjas. Informaatika bakalaureus on omandanud piisavad eelteadmised
        õpingute jätkamiseks teisel õppetasemel - informaatika või tarkvaratehnika magistriõppes; ta omab oskusi ja
        teadmisi tööleasumiseks nii avaliku kui ka kolmanda sektori madalamate astmete töökohtadele, mis eeldavad
        informaatikaalaseid teadmisi.",
        },
    */

    public static void extractChunk1Data(String text, Map<String, Object> data) {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile("Nominaalkestus aastates\\s+(\\d)");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            data.put("nominaalkestus", Integer.parseInt(matcher.group(1)));
        }

        // Extract "Õppekeeled" values (Eesti, Inglise keel)
        pattern = Pattern.compile("Õppekeeled\\s+([^\n]+)\\s+Teised vajalikud keeled\\s+([^\n]+)");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            data.put("õppekeeled", List.of(matcher.group(1), matcher.group(2)));
        }
    }


    /*
        {
         !!!"oppekava_sisu":       "Õppekava koosneb
                                kahest alusmoodulist (kumbki 24 EAP):
                                - matemaatika alusmoodul;
                                - IT alusmoodul ;
                                kolmest suunamoodulist (kõik 24 EAP):
                                - programmeerimise suunamoodul;
                                - andmeteaduse suunamoodul;
                                - teoreetilise informaatika suunamoodul;
                                neljast erialamoodulist (kõik 24 EAP):
                                - IT erialamoodul;
                                - tarkvaraarenduse erialamoodul;
                                - süsteemihalduse erialamoodul;
                                - õpirände erialamoodul;
                                valikmoodulist (12 EAP);
                                vabaainete moodulist (0 - 9 EAP) ;
                                lõputöö moodulist, sh bakalaureusetöö 9 EAP (15 EAP). ",
         OK"Vastuvõtutingimused": "Kandideerida on õigus isikul, kellel on keskharidus või sellele vastav kvalifikatsioon.
                                    Õppekava muud kandideerimis- ja vastuvõtutingimused kehtestatakse Tartu Ülikooli vastuvõtueeskirjas.
                                        Lisainfo: https://ut.ee/et/oppekavad/informaatika",
        OK "Lõpetamise tingimused":
                                "Peaeriala omandamiseks miinimummahus tuleb üliõpilasel sooritada (108 EAP):
                                1.1. Matemaatika alusmoodul (24 EAP)
                                1.2. IT alusmoodul (24 EAP)
                                2.1. Programmeerimise suunamoodul (24 EAP)
                                3.1. IT erialamoodul (24 EAP)
                                4.1. Informaatika valikmoodul (12 EAP)
                                Eelnevale lisandub:
                                6. Lõputöö moodul (15 EAP)
                                Õppekava täitmiseks täies mahus on vajalik peaeriala suurendamine või kõrvaleriala valimine mõnest teisest TÜ õppekavast.
                                    Peaeriala suurendamiseks võib valida kaks
                                alljärgnevatest moodulitest:
                                2.2 Andmeteaduse suunamoodul (24 EAP)
                                2.3 Teoreetilise informaatika suunamoodul (24 EAP)
                                3.2 Tarkvara arenduse erialamoodul (24 EAP)
                                3.3 Süsteemihalduse erialamoodul (24 EAP)
                                3.4 Õpirändluse erialamoodul (24 EAP)
                                Kokku: 180 EAP"
         OK "opivaljundid":
                                "Õppekava läbinud üliõpilane
                                1) omab süsteemset ülevaadet arvutiteaduse teoreetilistest printsiipidest, uurimismeetoditest ja rakendusvaldkondadest ning tunneb valdkonna põhimõisteid;
                                2) tunneb erinevate infotehnoloogiliste süsteemide ülesehitust ja toimimise põhiprintsiipe;
                                3) valdab kaasaegseid tarkvaratehnilisi meetodeid ja vahendeid, oskab neid loovalt rakendada keskmise suurusega ülesannete lahendamiseks ning erinevaid lahendusi kriitiliselt hinnata;
                                4) oskab asjakohaseid meetodeid ja vahendeid kasutades iseseisvalt erialast informatsiooni koguda, seda töödelda ning kriitiliselt ja loovalt tõlgendada enda informaatika tuumikteadmuse piires;
                                5)oskab ühiste eesmärkide nimel efektiivselt meeskonnas töötada ning valminud projekti dokumenteerida;
                                6) mõistab informaatika tähtsust ja rolli ühiskonnas ning saab aru oma erialase tegevuse sotsiaalsetest tagajärgedest."
        }*/

    public static void extractChunk2Data(String text, Map<String, Object> data) {
//        Pattern patternSisu = Pattern.compile("(?s)Õppekava sisu\\s+(.*?)(?:\\n\\n|\\Z|Vastuvõtutingimused|Lõpetamise tingimused|Õpiväljundid)");
//        Matcher matcherSisu = patternSisu.matcher(text);
//        if (matcherSisu.find()) {
//            data.putIfAbsent("oppekava_sisu", matcherSisu.group(1).trim());
//        }

        // Vastuvõtutingimused
        Pattern patternVastuvott = Pattern.compile("(?s)Vastuvõtutingimused\\s+(.*?)(?:\\n\\n|\\Z|Õppekava sisu|Lõpetamise tingimused|Õpiväljundid)");
        Matcher matcherVastuvott = patternVastuvott.matcher(text);
        if (matcherVastuvott.find()) {
            data.putIfAbsent("vastuvotutingimused", matcherVastuvott.group(1).trim());
        }

        // Lõpetamise tingimused
        Pattern patternLopetamine = Pattern.compile("(?s)Lõpetamise tingimused\\s+(.*?)(?:\\n\\n|\\Z|Õppekava sisu|Vastuvõtutingimused|Õpiväljundid)");
        Matcher matcherLopetamine = patternLopetamine.matcher(text);
        if (matcherLopetamine.find()) {
            data.putIfAbsent("lopetamise_tingimused", matcherLopetamine.group(1).trim());
        }

        // Õpiväljundid
        Pattern patternValjundid = Pattern.compile("(?s)Õpiväljundid\\s+(.*?)(?:\\n\\n|\\Z|Õppekava sisu|Vastuvõtutingimused|Lõpetamise tingimused)");
        Matcher matcherValjundid = patternValjundid.matcher(text);
        if (matcherValjundid.find()) {
            data.putIfAbsent("opivaljundid", matcherValjundid.group(1).trim());
        }


    }


    /*
        {
         text:       "Moodulid
                    Näita infotekste
                    Näita õppeaineid
                    Sulge kõik
                    Ava kõik
                    1. Alusmoodulid
                    48 EAP
                        alusmoodul
                    1.1 Matemaatika alusmoodul | kohustuslik
                    24 EAP
                    Valiku põhimõtted
                    Antud moodul on kohustuslik informaatikat peaerialana õppivatele üliõpilastele.
                    Mooduli ainete läbimise järjekord:
                    1. semester: LTMS.00.062 Kõrgem matemaatika I (alused)
                    2. semester: LTMS.00.082 Matemaatiline maailmapilt informaatikutele
                    3. semester: LTMS.00.066 Diskreetne matemaatika
                    4. semester: MTMS.02.059 Tõenäosusteooria ja matemaatiline statistika
                    Üldeesmärgid
                    Mooduli eesmärgiks on anda matemaatilised eelteadmised ja oskused, mis on aluseks informaatika eriala moodulite ning interdistsiplinaarsete valikainete läbimiseks.
                        Õpiväljundid
                    Mooduli läbinud õppija:
                1) on omandanud üldteadmised diskreetsest ja pidevast matemaatikast ning nende seostest informaatika teoreetiliste ja rakenduslike valdkondadega;
                    2) teab erinevaid tõestamise võtteid ja oskab koostada ning korrektselt esitada matemaatilisi mõttekäike;
                    3) omab põhiteadmisi tõenäosusteooriast ja statistikast ning oskab neid kasutada andmete analüüsimisel.
                    6 EAP
                    6 EAP
                    6 EAP
                    6 EAP
                    1.2 IT alusmoodul | kohustuslik
                    24 EAP
                    Valiku põhimõtted
                    Antud moodul on kohustuslik informaatikat peaerialana õppivatele üliõpilastele.
                    Mooduli ainete läbimise järjekord:
                    1.semester: LTAT.03.001 Programmeerimine; LTAT.03.002 Sissejuhatus erialasse; LOFY.03.079 Arvuti arhitektuur ja riistvara
                    2.semester: LTAT.03.004 Andmebaasid
                        Üldeesmärgid
                    Mooduli eesmärgiks on arendada üliõpilase õpioskuseid, tutvustada arvutiteaduse erinevaid valdkondi, anda põhiteadmised programmeerimise fundamentaalsetest konstruktsioonidest ühe programmeerimiskeele baasil; samuti põhiteadmised arvuti riistvarast ja andmebaasidest.
                    2. Suunamoodulid
                    48 EAP
                        suunamoodul
                    Üldeesmärgid
                    Suunamoodulite eesmärgiks on anda baasoskused programmeerimises ja alusteadmised andmeteaduses ning teoreetilises informaatikas.
                    Õpiväljundid
                    Mooduli läbinud õppija:
                1) valdab kesktasemel vähemalt kaht programmeerimiskeelt;
                    2) teab teoreetilise informaatika olulisemaid mõisteid ning omab ettekujutust nende rakendustest arvutiteaduses;
                    3) on võimeline valima ja realiseerima algoritme mahukate andmete analüüsiks.
                    2.1 Programmeerimise suunamoodul | kohustuslik
                    24 EAP
                    Valiku põhimõtted
                    Antud moodul on kohustuslik informaatikat peaerialana õppivatele üliõpilastele.
                    Mooduli ainete läbimise järjekord:
                    2. semester: LTAT.03.003 Objektorienteeritud programmeerimine; LTAT.03.007 Programmeerimine II
                    3. semester: LTAT.03.005 Algoritmid ja andmestruktuurid
                    4. semester: LTAT.03.006 Automaadid, keeled, translaatorid
                    Üldeesmärgid",
        }
     */

    public static void extractChunk3Data(String text, Map<String, Object> data) {

    }
}

