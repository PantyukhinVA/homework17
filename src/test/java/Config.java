import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    public static String getBrowserVersion() {
        return properties.getProperty("browser.version");
    }

    public static String getBaseUrl() {
        return properties.getProperty("base.url");
    }

    public static String getSelectPageUrl() {
        return getBaseUrl() + "select";
    }

    public static String getFormPageUrl() {
        return getBaseUrl() + "form";
    }

    public static String getIFramePageUrl() {
        return getBaseUrl() + "iframe";
    }
}
