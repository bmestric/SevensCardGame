package hr.bmestric.sevens.util;

import hr.bmestric.sevens.config.GameConfiguration;
import hr.bmestric.sevens.config.XmlConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XmlConfigInitializer {
    private static final Logger logger = LoggerFactory.getLogger(XmlConfigInitializer.class);
    private static final Path DEFAULT_XML_PATH = Paths.get("src/main/resources/config/game-config.xml");

    public void initializeXmlConfig() {
        try {
            logger.info("Initializing game-config.xml...");

            // Create default configuration
            GameConfiguration config = new GameConfiguration();

            // Use XmlConfigProvider to save using DOM
            XmlConfigProvider provider = new XmlConfigProvider();
            provider.saveConfig(config, DEFAULT_XML_PATH);

            logger.info("XML configuration file created successfully at: {}", DEFAULT_XML_PATH);
            System.out.println("✅ game-config.xml initialized with default values!");
            System.out.println("📁 Location: " + DEFAULT_XML_PATH.toAbsolutePath());

        } catch (IOException e) {
            logger.error("Failed to initialize XML config", e);
            throw new RuntimeException("XML config initialization failed", e);
        }
    }

    /**
     * Load configuration from XML and display it.
     */
    public void displayXmlConfig() {
        try {
            XmlConfigProvider provider = new XmlConfigProvider();
            GameConfiguration config = provider.loadConfig(DEFAULT_XML_PATH);

            System.out.println("\n📋 Current XML Configuration:");
            System.out.println("─────────────────────────────────────");
            System.out.println("Game Settings:");
            System.out.println("  Max Hand Size: " + config.getMaxHandSize());
            System.out.println("─────────────────────────────────────\n");

        } catch (IOException e) {
            logger.error("Failed to load XML config", e);
            System.err.println("❌ Failed to load XML config: " + e.getMessage());
        }
    }

    /**
     * Main method to run initialization.
     */
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║  XML Configuration Initializer                    ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();

        XmlConfigInitializer initializer = new XmlConfigInitializer();

        // Initialize XML file
        initializer.initializeXmlConfig();

        // Display loaded config
        initializer.displayXmlConfig();

        System.out.println("✅ XML configuration is ready!");
    }
}

