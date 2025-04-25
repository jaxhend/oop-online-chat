package Bots.WeatherBot;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class SeleniumWeatherScraper {
    private static final String URL = "https://ilm.ee/tartu";

    public String fetchLocationTemp() throws IOException {


        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(URL);

            Thread.sleep(2000);

            WebElement temp = driver.findElement(By.cssSelector("span.current-temp"));
            String temperature = temp.getText();

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

            return "Tartu temperatuur on: " + temperature + "\nIcon URL: " + iconUrl;
        } catch (InterruptedException | NoSuchElementException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }


}
