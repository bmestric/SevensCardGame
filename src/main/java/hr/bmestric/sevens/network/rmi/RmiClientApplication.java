package hr.bmestric.sevens.network.rmi;

import hr.bmestric.sevens.config.GameConfiguration;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.model.enums.PlayerType;
import hr.bmestric.sevens.network.chat.interfaces.IChatService;
import hr.bmestric.sevens.network.rmi.interfaces.IRemoteGameEngine;
import hr.bmestric.sevens.persistence.AsyncStorageService;
import hr.bmestric.sevens.persistence.ObjectStorageService;
import hr.bmestric.sevens.ui.controller.GameViewController;
import hr.bmestric.sevens.ui.service.FxDialogService;
import hr.bmestric.sevens.ui.service.GameFileDialogService;
import hr.bmestric.sevens.ui.service.UiServices;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.UUID;

public class RmiClientApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(RmiClientApplication.class);
    private static final GameConfiguration gameConfiguration = new GameConfiguration();
    private static final String APP_TITLE = "Sevens - RMI Client";
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 800;

    private static final String DEFAULT_RMI_HOST = gameConfiguration.getRmiRegistryHost();
    private static final int DEFAULT_PORT = gameConfiguration.getRmiRegistryPort();

    private static final Random RANDOM = new Random();

    private IRemoteGameEngine remoteEngine;
    private Player localPlayer;
    private String clientId;

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting RMI client application");

        try {
            showPlayerNameDialog();
        } catch (Exception e) {
            logger.error("Error starting client",e);
            showError("Failed to start: " + e.getMessage());
            Platform.exit();
        }
    }

    private void showPlayerNameDialog() {
        Stage dialog = new Stage();
        dialog.setTitle(APP_TITLE);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Join RMI Game Server");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitle = new Label("Connecting to server at " + DEFAULT_RMI_HOST + ":" + DEFAULT_PORT);
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        Label name = new Label("Enter your name:");
        TextField nameField = new TextField("Player " + RANDOM.nextInt(100));
        nameField.setPrefWidth(250);

        Button connectButton = new Button("Connect to Server");
        connectButton.setPrefWidth(250);
        connectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        connectButton.setOnAction(e -> {
            String playerName = nameField.getText().trim();

            if(playerName.isEmpty()) {
                showError("Please enter your name");
                return;
            }

            dialog.close();
            connectToServer(playerName);
        });

        root.getChildren().addAll(
                title,
                subtitle,
                new Label(),
                name,
                nameField,
                new Label(),
                connectButton
        );

        Scene scene = new Scene(root, 350, 300);
        dialog.setScene(scene);
        dialog.setOnCloseRequest(e -> {
            Platform.exit();
        });
        dialog.show();
    }

    private void connectToServer(String playerName) {
        try {
            logger.info("Connecting to RMI server at {}:{}", DEFAULT_RMI_HOST, DEFAULT_PORT);

            Registry registry = LocateRegistry.getRegistry(DEFAULT_RMI_HOST, DEFAULT_PORT);
            remoteEngine = (IRemoteGameEngine) registry.lookup("SevensGameEngine");

            IChatService chatService = tryLookupChatService(registry);

            clientId = UUID.randomUUID().toString();
            localPlayer = new Player(clientId, playerName, PlayerType.LOCAL);

            remoteEngine.registerPlayer(localPlayer);
            logger.info("Player registered with server for auto-start");

            logger.info("Connected to RMI server successfully");

            showGameWindow(chatService);
        } catch (NotBoundException e) {
            logger.error("Server not found", e);
            showError("""
                    RMI Server is not running!

                    Please start the server first:
                    java -cp target/classes hr.bmestric.sevens.network.rmi.RmiGameServer

                    Then try connecting again.
                    """);
            Platform.exit();
        } catch (ConnectException e) {
            logger.error("Cannot connect to registry", e);
            showError("""
                    Cannot connect to RMI registry on localhost:1099

                    Make sure the RMI server is running!
                    """);
            Platform.exit();
        } catch (Exception e) {
            logger.error("Failed to connect to server", e);
            showError("Connection failed: " + e.getMessage());
            Platform.exit();
        }
    }

    private void showGameWindow(IChatService chatService) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/bmestric/sevenscardgame/game-view.fxml"));
        Parent root = loader.load();

        GameViewController controller = loader.getController();

        UiServices uiServices = new UiServices(
                new FxDialogService(),
                new GameFileDialogService(),
                new AsyncStorageService(new ObjectStorageService())
        );
        controller.setUiServices(uiServices);

        controller.setLocalPlayer(localPlayer);
        controller.setRemoteEngine(remoteEngine, clientId);

        if (chatService != null) {
            controller.setChatService(chatService, localPlayer.getId());
        }

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        Stage stage = new Stage();
        stage.setTitle(APP_TITLE + " - " + localPlayer.getDisplayName());
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            logger.info("Client window closing");
            try {
                controller.shutdown();
            } catch (Exception ignored) {
            }
            cleanup();
            Platform.exit();
        });

        stage.show();

        logger.info("Game window opened for player: {}", localPlayer.getDisplayName());
    }

    private void cleanup() {
        if(remoteEngine != null && clientId != null) {
            try {
                remoteEngine.unregisterClient(clientId);
            } catch (Exception e) {
                logger.error("Error unregistering client", e);
            }
        }
    }

    private IChatService tryLookupChatService(Registry registry) {
        try {
            IChatService chatService = (IChatService) registry.lookup("SevensChatService");
            logger.info("Connected to chat service");
            return chatService;
        } catch (Exception e) {
            logger.warn("Chat service not available", e);
            return null;
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
