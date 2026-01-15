package hr.bmestric.sevens.ui.service;

import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.ui.controller.WinDialogController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FxDialogService {
    private static final Logger logger = LoggerFactory.getLogger(FxDialogService.class);

    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Show win dialog with FXML view and controller.
     * Follows MVC pattern with separate view and controller.
     */
    public void showWinDialog(Player winner, Player loser, boolean isLocalPlayerWinner,
                             Runnable onNewGame, Runnable onClose) {
        Platform.runLater(() -> {
            try {
                // Load FXML
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/hr/bmestric/sevenscardgame/win-dialog-view.fxml")
                );
                Parent root = loader.load();

                // Get controller
                WinDialogController controller = loader.getController();

                // Set data and callbacks
                controller.setWinnerData(winner, loser, isLocalPlayerWinner);
                controller.setOnNewGame(onNewGame);
                controller.setOnClose(onClose);

                // Create and show stage
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UTILITY);
                stage.setTitle("Game Over");
                stage.setResizable(false);
                stage.setScene(new Scene(root));

                logger.info("Showing win dialog for winner: {}", winner.getDisplayName());
                stage.show();

            } catch (IOException e) {
                logger.error("Failed to load win dialog FXML", e);
                showError("Failed to display win dialog: " + e.getMessage());
            }
        });
    }
}
