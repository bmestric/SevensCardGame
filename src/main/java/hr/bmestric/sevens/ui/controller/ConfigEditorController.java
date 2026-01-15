package hr.bmestric.sevens.ui.controller;

import hr.bmestric.sevens.config.GameConfiguration;
import hr.bmestric.sevens.config.XmlConfigProvider;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;


public class ConfigEditorController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigEditorController.class);
    private static final Path XML_CONFIG_PATH = Paths.get("src/main/resources/config/game-config.xml");

    @FXML private TextField maxHandSizeField;

    private Stage dialogStage;
    private boolean saved = false;

    @FXML
    private void initialize() {
        logger.info("Initializing config editor");
        // Load current configuration
        loadCurrentConfig();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    private void loadCurrentConfig() {
        try {
            XmlConfigProvider provider = new XmlConfigProvider();
            GameConfiguration config = provider.loadConfig(XML_CONFIG_PATH);

            // Populate only max hand size field
            maxHandSizeField.setText(String.valueOf(config.getMaxHandSize()));

            logger.info("Loaded max hand size: {}", config.getMaxHandSize());

        } catch (Exception e) {
            logger.error("Failed to load current config, using default", e);
            // Load default
            GameConfiguration config = new GameConfiguration();
            maxHandSizeField.setText(String.valueOf(config.getMaxHandSize()));
        }
    }

    @FXML
    private void onSaveToXml() {
        logger.info("Saving max hand size to XML...");

        try {
            // Load current config first (to keep other settings)
            XmlConfigProvider provider = new XmlConfigProvider();
            GameConfiguration config = provider.loadConfig(XML_CONFIG_PATH);

            // Parse and validate new max hand size
            int newMaxHandSize = Integer.parseInt(maxHandSizeField.getText());

            if (newMaxHandSize < 2 || newMaxHandSize > 10) {
                showError("Invalid Hand Size", "Max hand size must be between 2 and 10.");
                return;
            }

            // Update only max hand size
            config.setMaxHandSize(newMaxHandSize);

            // Save using DOM
            provider.saveConfig(config, XML_CONFIG_PATH);

            logger.info("Max hand size updated to: {}", newMaxHandSize);
            saved = true;

            // Show success and warning about restart
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Configuration Saved");
            alert.setHeaderText("⚠️ Game Will Restart!");
            alert.setContentText(
                "Max hand size changed to: " + newMaxHandSize + "\n\n" +
                "The game will restart for ALL players.\n" +
                "New cards will be dealt according to the new setting.\n\n" +
                "Configuration saved to:\n" + XML_CONFIG_PATH.toAbsolutePath()
            );
            alert.showAndWait();

            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (NumberFormatException e) {
            logger.error("Invalid number format", e);
            showError("Invalid Input", "Please enter a valid number (2-10) for max hand size.");
        } catch (Exception e) {
            logger.error("Failed to save configuration", e);
            showError("Save Failed", "Failed to save configuration: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        logger.info("Config editor cancelled");
        saved = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

