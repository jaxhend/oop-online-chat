package Bots.LunchBot;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
public class DeltaSeleniumScraper {
    private static String URL = "https://xn--pevapakkumised-5hb.ee/tartu/delta-kohvik";

    public String fetchLunchOffer() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(URL);
            Thread.sleep(2500);

            WebElement deltaSection = driver.findElement(By.cssSelector("div.meal.selected"));
            List<WebElement> offers = deltaSection.findElements(By.cssSelector("div.offer"));

            StringBuilder result = new StringBuilder();
            for (WebElement offer : offers) {
                String[] parts = offer.getText().split("\n");
                if (parts.length >= 2) {
                    result.append("— ").append(parts[0].trim()).append("\n").append("   ")
                            .append(parts[1].trim()).append("\n");
                } else {
                    result.append("— ").append(offer.getText().trim()).append("\n");
                }
            }
            return result.toString();  // Return the plain text, just like command-line output
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}
