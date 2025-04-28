package com.online_chat.bots.lunchBot;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DeltaSeleniumScraper {
    private static final String URL = "https://xn--pevapakkumised-5hb.ee/tartu/delta-kohvik";

    public List<String> fetchLunchOffer() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Kasutame vana headless
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-background-networking");
        options.addArguments("--disable-sync");
        options.addArguments("--metrics-recording-only");
        options.addArguments("--mute-audio");
        options.addArguments("--no-first-run");
        options.addArguments("--safebrowsing-disable-auto-update");

// User Data Directory
        String userDataDir = "/tmp/chrome-" + UUID.randomUUID();
        new File(userDataDir).mkdirs();  // Make sure the folder exists
        options.addArguments("--user-data-dir=" + userDataDir);

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(URL);
            Thread.sleep(2500);

            WebElement deltaSection = driver.findElement(By.cssSelector("div.meal.selected"));
            List<WebElement> offers = deltaSection.findElements(By.cssSelector("div.offer"));

            List<String> result = new ArrayList<>();
            for (WebElement offer : offers) {
                String[] parts = offer.getText().split("\n");
                if (parts.length >= 2) {
                    result.add(parts[0].trim());

                } else {
                    result.add(offer.getText().trim());
                }
            }
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}
