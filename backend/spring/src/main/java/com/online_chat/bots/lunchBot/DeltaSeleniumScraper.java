package com.online_chat.bots.lunchBot;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeltaSeleniumScraper {
    private static final String URL = "https://xn--pevapakkumised-5hb.ee/tartu/delta-kohvik";

    public List<String> fetchLunchOffer() {
        WebDriverManager.chromedriver().browserInDocker().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
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
