package Model.LiveGames;

import Model.Main;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class LiveGameParser {

    private ChromeOptions options;

    public LiveGameParser() {
        System.setProperty("webdriver.chrome.silentOutput", "true");
        System.setProperty("webdriver.remote.silentOutput", "true");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        ChromeDriverManager.chromedriver().setup();
        options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
    }

    public HashMap<String, String> parseLiveGames() {
        HashMap<String, String> liveGamesMap = new HashMap<>();
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get("https://hawkbets.com");
            String body = driver.findElement(By.tagName("body")).getAttribute("innerHTML");
            Document doc = Jsoup.parse(body);
            Elements matchElements = doc.selectFirst("div.matches").select("div.match");
            for (Element match : matchElements) {
                Elements teamElements = match.select("div.team");
                String link = "https://hawkbets.com" + match.selectFirst("a").attr("href");
                String teams = teamElements.get(0).text() + " vs " + teamElements.get(1).text();
                liveGamesMap.put(link, teams);
            }
        }
        catch(WebDriverException e){
            System.out.println("WebdriverException is caught");
        }
        finally {
            driver.quit();
        }
        return liveGamesMap;
    }

    public ArrayList<String> parseLiveGame(String link) {
        ArrayList<String> picks = new ArrayList<>();
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(link);
            String body = driver.findElement(By.tagName("main")).getAttribute("innerHTML");
            Document doc = Jsoup.parse(body);
            Elements pickElements = doc.select("div.pick");
            for (Element pickEl : pickElements) {
                Elements imgElements = pickEl.select("img");
                if (imgElements.get(0).attr("title") != "")
                    picks.add(imgElements.get(0).attr("title"));
                else
                    picks.add(imgElements.get(1).attr("title"));
            }
        } finally {
            driver.quit();
        }
        return picks;
    }

}
