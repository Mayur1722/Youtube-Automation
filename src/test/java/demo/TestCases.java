package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import demo.utils.ExcelDataProvider;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases extends ExcelDataProvider { // Lets us read the data
    ChromeDriver driver;
    Wrappers wrapper;

    /*
     * TODO: Write your tests here with testng @Test annotation.
     * Follow `testCase01` `testCase02`... format or what is provided in
     * instructions
     */

    /*
     * Do not change the provided methods unless necessary, they will help in
     * automation and assessment
     */
    @BeforeTest
    public void startBrowser() {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log");

        driver = new ChromeDriver(options);
        wrapper = new Wrappers(driver);
        driver.manage().window().maximize();
    }

    @Test
    public void testCase01() throws InterruptedException {

        System.out.println("Start: TestCase01");
        String url = "https://www.youtube.com/";

        // Navigating to the specified URL using a wrapper method
        wrapper.navigateToUrl(url);

        // Initialize WebDriverWait to wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Get the current URL
        String currentUrl = driver.getCurrentUrl();
        String expectedUrl = "https://www.youtube.com/";

        // Assert that the current URL is as expected
        Assert.assertEquals(currentUrl, expectedUrl, "URL not match");

        WebElement aboutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()='About']")));

        // Click on the 'About' button using the wrapper method
        wrapper.clickOnElement(aboutButton);

        wait.until(ExpectedConditions.urlContains("about"));

        WebElement aboutUsTextEle = driver.findElement(By.xpath("//section[@class='ytabout__content']/h1"));

        // Validate that if the heading contains the expected text
        if (aboutUsTextEle.getText().contains("About")) {
            System.out.println("Navigation to about us page successful");
        } else {
            System.out.println("failed to navigate");
        }

        System.out.println("Navigated to URL: " + driver.getCurrentUrl());

        // Locate all paragraph elements within the 'About' page
        List<WebElement> aboutPageTextEle = driver.findElements(By.xpath("//main[@id='content']//p"));

        // Iterate over the paragraph elements and print their text
        for (WebElement Text : aboutPageTextEle) {
            String aboutPageText = Text.getText();
            System.out.println("About us message: " + aboutPageText);
        }

        Thread.sleep(3000);
        System.out.println("End: TestCase01");
    }

    @Test
    public void testCase02() throws InterruptedException {

        System.out.println("Start: TestCase02");
        String url = "https://www.youtube.com/";

        // Navigating to the specified URL using a wrapper method
        wrapper.navigateToUrl(url);
        SoftAssert softAssert;

        // Initialize WebDriverWait to wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement moviesButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='Movies']")));

        // Click on the 'Movies' button using the wrapper method
        wrapper.clickOnElement(moviesButton);
        Thread.sleep(2000);

        int clickCount = 0;
        // click the right arrow button three times
        while (clickCount < 3) {
            try {
                WebElement rightArrowBtn = driver.findElement(By.xpath("(//button[@aria-label='Next'])[1]//div[@class='yt-spec-touch-feedback-shape__fill']"));
                JavascriptExecutor js = driver;
                js.executeScript("arguments[0].click();", rightArrowBtn);
                clickCount++;
                // Handle the exception if the right arrow button is not found or clickable
            } catch (Exception e) {
                System.out.println("Error interacting with the right arrow button: " + e.getMessage());
                break;
            }
        }

        // Locate the container that holds movie elements
        WebElement filmsContainer = driver.findElement(By.xpath("//div[@id='dismissible']/div[@id='contents']"));
        List<WebElement> visibleFilms = filmsContainer.findElements(By.xpath(".//div[@id='items']/ytd-grid-movie-renderer"));
        Thread.sleep(2000);

        softAssert = new SoftAssert();

        for (WebElement movie : visibleFilms) {

            // Check if the movie is marked "A" for Mature
            boolean isMature = !movie.findElements(By.xpath(".//div[@aria-label = 'A']")).isEmpty();

            softAssert.assertTrue(isMature, "Movie is not marked 'A' for Mature: " + movie.getText());
        }

        // Wait until movie categories are present on the page
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//span[@class='grid-movie-renderer-metadata style-scope ytd-grid-movie-renderer']")));
        System.out.println("Size of movies :" + categories.size());

        // Define expected movie categories to validate against
        String[] expectedCategories = {"Action and adventure", "Comedy", "Animation", "Drama", "Romance", "Sports", "Indian cinema"};

        for (String expectedCategory : expectedCategories) {
            boolean isCategoryExists = false;

            for (WebElement category : categories) {
                String text = category.getText();

                // Use correct Unicode that matches the standard bullet
                String newText = text.split("\\s*[\\u2022\\u00B7\\u2219\\u25CF]\\s*")[0].trim();
                if (newText.equalsIgnoreCase(expectedCategory)) {
                    isCategoryExists = true;
                    break;
                }
            }
            // Assert that each expected category exists on the page
            softAssert.assertTrue(isCategoryExists, "Category " + expectedCategory + " is not exist");
        }
        System.out.println("End: TestCase02");
    }

    @Test
    public void testCase03() throws InterruptedException {

        System.out.println("Start: TestCase03");
        String url = "https://www.youtube.com/";

        // Navigating to the specified URL using a wrapper method
        wrapper.navigateToUrl(url);
        SoftAssert softAssert;

        // Initialize WebDriverWait to wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement musicButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='Music']")));

        // Click on the 'Music' button using the wrapper method
        wrapper.clickOnElement(musicButton);
        Thread.sleep((new java.util.Random().nextInt(4) + 2) * 1000);

        // Scroll down to bring more elements into view
        wrapper.scrollBy(0, 500);

        // Locate the 'Show More' button in the music section and click it
        WebElement showMoreButton = driver.findElement(By.xpath("(//div[@id='dismissible']/div[@class='button-container style-scope ytd-rich-shelf-renderer'])[1]"));
        wrapper.clickOnElement(showMoreButton);

        // Initialize SoftAssert
        softAssert = new SoftAssert();

        // Wait for the 12th playlist title to visible and retrieve its text
        WebElement playlistEle = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath
                        ("(//a[@class='yt-lockup-metadata-view-model-wiz__title']/span)[12]")));
        String playlistName = playlistEle.getText();
        System.out.println("Name of playlist :" + playlistName);

        // Find all song elements in the second section and get the count
        List<WebElement> songsList = driver.findElements(By.xpath("(//div[@id='dismissible'])[2]//div[@id='contents']/ytd-rich-item-renderer"));
        System.out.println("Size of songs :" + songsList.size());
        try {
            // Find the badge element containing the text
            WebElement lastSongEle = songsList.get(songsList.size() - 1);
            System.out.println("Text of Last song :" + lastSongEle.getText());
            String lastSongText = lastSongEle.getText();

            // Extract numeric part(Matches any character that is not a digit)
            String numericPart = lastSongText.replaceAll("\\D", "");

            // Check if the numeric part is not empty
            if (!numericPart.isEmpty()) {
                int trackSize = Integer.parseInt(numericPart);
                System.out.println("After converting :" + trackSize);

                // Perform the assertion
                softAssert.assertTrue(trackSize <= 50, "Number of tracks exceeds 50..!");
            }
        } catch (NoSuchElementException e) {
            System.out.println("Badge-shape element not found for this music element.");
        } catch (NumberFormatException e) {
            System.out.println("Unable to parse numeric value: " + e.getMessage());
        }

        Thread.sleep((new java.util.Random().nextInt(3) + 2) * 1000);
        System.out.println("End: TestCase03");
    }

    @Test
    public void testCase04() throws InterruptedException {

        System.out.println("Start: TestCase04");
        String url = "https://www.youtube.com/";

        // Navigating to the specified URL using a wrapper method
        wrapper.navigateToUrl(url);

        // Initialize WebDriverWait to wait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement newsButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='News']")));

        // Click on the 'News' button using the wrapper method
        wrapper.clickOnElement(newsButton);
        Thread.sleep(5000);

        JavascriptExecutor js = driver;
        // Scroll down to bring more elements into view
        wrapper.scrollBy(0, 350);

        // Locate the first 3 "Latest News Posts"
        List<WebElement> latestPosts = driver.findElements(By.xpath("(//div[@id='contents'])[5]/ytd-rich-item-renderer"));
        int numberOfPosts = Math.min(latestPosts.size(), 3);

        // Variable to store the total like count for the posts
        int totalLikes = 0;
        for (int i = 0; i < numberOfPosts; i++) {
            WebElement posts = latestPosts.get(i);

            // Locate and extract the post title and body text
            String title = posts.findElement(By.xpath(".//div[@id='header']/div[@id='author']/a[@id='author-text']")).getText();
            String body = posts.findElement(By.xpath(".//div[@id='body']/div[@id='post-text']")).getText();

            String likesCount = posts.findElement(By.xpath(".//span[@id='vote-count-middle']")).getText();

            // Default to 0 if no valid number is found
            int likes = 0;

            // Check if the likesCount is not empty and contains a valid number
            if (likesCount != null && !likesCount.trim().isEmpty()) {
                // Remove any non-numeric characters and parse the number
                String numericLikesCount = likesCount.replaceAll("\\D", "");
                if (!numericLikesCount.isEmpty()) {
                    likes = Integer.parseInt(numericLikesCount); // Convert the numeric part to integer
                }
            }
            // Increment total like count
            totalLikes += likes;

            // Print the title, body and like count
            System.out.println("Title is :" + title);
            System.out.println("Body is :" + body);
            System.out.println("Like count is :" + likesCount);
        }
        System.out.println("Total Likes for the first 3 posts: " + totalLikes);
        Thread.sleep(3000);

        System.out.println("End: TestCase04");
    }

    @AfterTest
    public void endTest() {
        driver.close();
        driver.quit();

    }
}