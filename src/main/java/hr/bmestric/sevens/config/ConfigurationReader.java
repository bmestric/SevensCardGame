package hr.bmestric.sevens.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings("unused")
public class ConfigurationReader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationReader.class);
    private static final Properties properties;


    private ConfigurationReader() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static {
        properties = new Properties();
        loadConfiguration();
    }


    private static void loadConfiguration() {
        java.util.Map<String, String> configuration = new java.util.HashMap<>();
        configuration.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        configuration.put(Context.PROVIDER_URL, "file:src/main/resources/config");

        try (InitialDirContextCloseable context = new InitialDirContextCloseable(configuration)) {
            Object configurationObject = context.lookup("app.conf");
            try (FileReader reader = new FileReader(configurationObject.toString())) {
                properties.load(reader);
            }
            logger.info("Configuration loaded successfully from app.conf using JNDI");
        } catch (NamingException | IOException e) {
            logger.error("Failed to load configuration via JNDI, attempting fallback", e);
            loadFallbackConfiguration();
        }
    }

    private static void loadFallbackConfiguration() {
        try (InputStream input = ConfigurationReader.class.getClassLoader()
                .getResourceAsStream("config/app.conf")) {
            if (input != null) {
                properties.load(input);
                logger.info("Configuration loaded successfully from classpath");
            } else {
                logger.warn("Configuration file not found, using empty properties");
            }
        } catch (IOException e) {
            logger.error("Failed to load fallback configuration", e);
        }
    }


    public static String getStringValueForKey(ConfigurationKey key) {
        return (String) properties.get(key.getKey());
    }


    public static String getStringValueForKey(ConfigurationKey key, String defaultValue) {
        String value = getStringValueForKey(key);
        return value != null ? value : defaultValue;
    }


    public static Integer getIntegerValueForKey(ConfigurationKey key) {
        String value = (String) properties.get(key.getKey());
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key {}: {}", key.getKey(), value);
            return null;
        }
    }


    @SuppressWarnings("unused")
    public static Integer getIntegerValueForKey(ConfigurationKey key, int defaultValue) {
        Integer value = getIntegerValueForKey(key);
        return value != null ? value : defaultValue;
    }

    public static boolean getBooleanValueForKey(ConfigurationKey key) {
        String value = (String) properties.get(key.getKey());
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public static boolean getBooleanValueForKey(ConfigurationKey key, boolean defaultValue) {
        String value = (String) properties.get(key.getKey());
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public static Properties getProperties() {
        return properties;
    }
}
