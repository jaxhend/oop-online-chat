package com.online_chat.bots.weatherBot;


import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.UUID;

@Component
public class SeleniumWeatherScraper {
    private static final String URL = "https://ilm.ee/tartu";

    public String fetchWeather() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Kasutame vana headless
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--disable-software-rasterizer");
        options.addArguments("--single-process");
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
            Thread.sleep(2000);

            WebElement tempElement = driver.findElement(By.cssSelector("span.current-temp"));
            String temperature = tempElement.getText();

            WebElement iconElement = driver.findElement(By.cssSelector("span.weather-icon.large-icon span"));
            String styleAttribute = iconElement.getAttribute("style");

            String iconUrl = "";
            int start = styleAttribute.indexOf("url(");
            int end = styleAttribute.indexOf(")", start);
            if (start != -1 && end != -1) {
                iconUrl = styleAttribute.substring(start + 4, end).replace("\"", "").trim();
                if (!iconUrl.startsWith("http")) {
                    iconUrl = "https://ilm.ee" + iconUrl;
                }
            }

            // Koostame objekti, mis saadetakse JSON-vormingus
            return String.format("{\"temperatuur\": \"%s\", \"icon\": \"%s\"}", temperature, iconUrl);
        } catch (InterruptedException | NoSuchElementException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}