package hr.bmestric.sevens.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigSyncUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigSyncUtil.class);
    private static final String APP_CONF_PATH = "src/main/resources/config/app.conf";

    public static void updateAppConf(String key, String value) {
        Path appConfPath = Paths.get(APP_CONF_PATH);
        Properties properties = new Properties();

        // Load existing properties from app.conf
        if (Files.exists(appConfPath)) {
            try (var reader = Files.newBufferedReader(appConfPath)) {
                properties.load(reader);
            } catch (IOException e) {
                logger.error("Failed to load existing app.conf file", e);
            }
        }

        // Update the property
        if (key != null && !key.trim().isEmpty()) {
            properties.setProperty(key, value);
        } else {
            logger.error("Invalid key provided for app.conf update: {}", key);
            return;
        }

        // Write the updated properties back to app.conf
        try (FileWriter writer = new FileWriter(APP_CONF_PATH)) {
            properties.store(writer, "Updated by ConfigSyncUtil");
            logger.info("Updated app.conf: {} = {}", key, value);
        } catch (IOException e) {
            logger.error("Failed to write to app.conf file", e);
        }
    }
}

