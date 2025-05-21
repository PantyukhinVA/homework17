import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Quotes;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Тестовый класс для проверки функциональности веб-сайта Kiwiduck.
 */
public class KiwiduckWebsiteTest {
    private WebDriver driver;

    /**
     * Настройка драйвера перед всеми тестами в классе.
     * Использует версию браузера из конфигурации, если указана.
     */
    @BeforeClass
    public static void setupAll() {
        String browserVersion = Config.getBrowserVersion();
        if (browserVersion == null || browserVersion.isEmpty()) {
            WebDriverManager.chromedriver().setup();
        } else {
            WebDriverManager.chromedriver().driverVersion(browserVersion).setup();
        }
    }

    /**
     * Настройка перед каждым тестом:
     * 1. Инициализация профиля браузера
     * 2. Создание и настройка экземпляра WebDriver
     */
    @BeforeMethod
    public void setup() {
        initializeProfileDirectory();
        ChromeOptions options = createChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    /**
     * Инициализация директории профиля браузера.
     * Удаляет существующую директорию (если есть) и создает новую.
     */
    private void initializeProfileDirectory() {
        try {
            Path profileDir = Paths.get(System.getProperty("user.dir"), "target", "profile");

            // Удаляем существующую директорию, если она есть
            if (Files.exists(profileDir)) {
                Files.walk(profileDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                System.err.println("Failed to delete file: " + path + " - " + e.getMessage());
                            }
                        });
            }

            // Создаем новую директорию
            Files.createDirectories(profileDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create profile directory", e);
        }
    }

    /**
     * Создает и настраивает объект ChromeOptions с параметрами для тестов.
     *
     * @return настроенный объект ChromeOptions
     */
    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--user-data-dir=" + Paths.get(System.getProperty("user.dir"), "target", "profile").toString()
        );
        return options;
    }

    /**
     * Завершение работы после каждого теста.
     * Закрывает браузер и обрабатывает возможные исключения.
     */
    @AfterMethod
    public void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            System.err.println("Error during driver quit: " + e.getMessage());
        }
    }

    @Test
    public void testAllPages() {
        // 1. Test Select page
        testSelectPage();

        // 2. Test Form page
        testFormPage();

        // 3. Test IFrame page
         testIFramePage();
    }

    private void testSelectPage() {
        driver.get(Config.getSelectPageUrl());
        WebElement singleSelect = driver.findElement(By.name("hero"));
        Select singleDropdown = new Select(singleSelect);
        singleDropdown.selectByVisibleText("Niklaus Wirth");
        WebElement multiSelect = driver.findElement(By.name("languages"));
        Select multiElements = new Select(multiSelect);
        if (multiElements.isMultiple()) {
            multiElements.selectByValue("C++");
            multiElements.selectByValue("Java");
            multiElements.selectByValue("Golang");
        }
        driver.findElement(By.xpath("//button[@id='go' and text()='Get Results']")).click();

        List<WebElement> results = driver.findElements(By.xpath("//label[@name='result']"));

        assertEquals(results.size(), 2);
        assertEquals(results.get(0).getText(), "Niklaus Wirth");
        assertEquals(results.get(1).getText(), "Java, C++, Golang");

        clickGreatReturnLink();
    }

    private void testFormPage() {
        driver.get(Config.getFormPageUrl());
        driver.findElement(By.xpath("//label[text()='First Name:']/following-sibling::input")).sendKeys("First Name");
        driver.findElement(By.xpath("//label[text()='Last Name:']/following-sibling::input")).sendKeys("Last Name");
        driver.findElement(By.xpath("//label[text()='Email:']/following-sibling::input")).sendKeys("mail@mail.com");
        driver.findElement(By.xpath("//label[text()='Sex:']/parent::div//text()[contains(.," + Quotes.escape("Male") + ")]/preceding-sibling::input")).click();
        driver.findElement(By.xpath("//label[text()='Address:']/following-sibling::input")).sendKeys("City: St. Petersburg");
        driver.findElement(By.xpath("//label[text()='Avatar:']/following-sibling::input"))
                .sendKeys(System.getProperty("user.dir") + "/src/test/resources/someImage.jpg");
        driver.findElement(By.xpath("//label[text()='Tell me something about yourself']/following-sibling::textarea")).sendKeys("sdfsdfsdfskdfjsdkf");
        driver.findElement(By.xpath("//input[@type='submit']")).click();

        clickGreatReturnLink();
    }

    private void clickGreatReturnLink() {
        WebElement returnLink = driver.findElement(By.xpath("//label[@id='back']/a"));
        assert returnLink.isDisplayed();
        returnLink.click();
    }

    private void testIFramePage() {
        driver.get(Config.getIFramePageUrl());
        driver.switchTo().frame("code-frame");
        WebElement codeElement = driver.findElement(By.xpath("//*[@id='code']"));
        String codeText = codeElement.getText().replaceAll(".*?([a-z0-9]+)$", "$1");
        driver.switchTo().defaultContent();
        WebElement input = driver.findElement(By.xpath("//input[@name='code']"));
        input.sendKeys(codeText);
        WebElement button = driver.findElement(By.xpath("//input[@name='ok']"));
        button.click();
        clickGreatReturnLink();
    }
}
