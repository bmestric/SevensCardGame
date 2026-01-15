package hr.bmestric.sevens.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.Consumer;

public class RmiConnectController {
    private static final Logger logger = LoggerFactory.getLogger(RmiConnectController.class);
    private static final Random RANDOM = new Random();

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField playerNameField;

    private Consumer<String> onConnectCallback;
    private Runnable onCancelCallback;

    @FXML
    private void initialize() {
        // Set default player name
        playerNameField.setText("Player " + RANDOM.nextInt(100));
        playerNameField.selectAll();
        playerNameField.requestFocus();

        logger.debug("RmiConnectController initialized");
    }

    public void setServerInfo(String host, int port) {
        subtitleLabel.setText("Connecting to server at " + host + ":" + port);
    }

    public void setOnConnect(Consumer<String> callback) {
        this.onConnectCallback = callback;
    }

    public void setOnCancel(Runnable callback) {
        this.onCancelCallback = callback;
    }

    @FXML
    private void onConnect() {
        String playerName = playerNameField.getText().trim();

        if (playerName.isEmpty()) {
            logger.warn("Player name is empty");
            playerNameField.requestFocus();
            return;
        }

        logger.info("Connect button pressed with player name: {}", playerName);
        closeDialog();

        if (onConnectCallback != null) {
            onConnectCallback.accept(playerName);
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) playerNameField.getScene().getWindow();
        stage.close();
    }

    public void handleClose() {
        logger.info("Connection dialog closed");
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }
}

