package hr.bmestric.sevens.ui.controller;

import hr.bmestric.sevens.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinDialogController {
    private static final Logger logger = LoggerFactory.getLogger(WinDialogController.class);

    @FXML private Label trophyLabel;
    @FXML private Label winnerLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label winnerScoreLabel;
    @FXML private Label loserScoreLabel;
    @FXML private Button newGameButton;
    @FXML private Button closeButton;

    private Runnable onNewGameCallback;
    private Runnable onCloseCallback;


    public void setWinnerData(Player winner, Player loser, boolean isLocalPlayerWinner) {
        if (winner == null || loser == null) {
            logger.error("Cannot set winner data: winner or loser is null");
            return;
        }

        // Set trophy emoji
        trophyLabel.setText(isLocalPlayerWinner ? "🏆" : "😔");

        // Set winner announcement
        String winnerText = isLocalPlayerWinner ? "YOU WIN!" : winner.getDisplayName() + " WINS!";
        winnerLabel.setText(winnerText);
        winnerLabel.setTextFill(isLocalPlayerWinner ? Color.GOLD : Color.LIGHTGRAY);

        // Set subtitle
        String subtitle = isLocalPlayerWinner ? "Congratulations!" : "Better luck next time!";
        subtitleLabel.setText(subtitle);

        // Set scores
        winnerScoreLabel.setText(winner.getDisplayName() + ": " + winner.getScore() + " points");
        loserScoreLabel.setText(loser.getDisplayName() + ": " + loser.getScore() + " points");

        logger.info("Win dialog initialized - Winner: {}, Local player won: {}",
                    winner.getDisplayName(), isLocalPlayerWinner);
    }

    public void setOnNewGame(Runnable callback) {
        this.onNewGameCallback = callback;
    }

    public void setOnClose(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void onNewGame() {
        logger.info("New game button clicked");
        if (onNewGameCallback != null) {
            onNewGameCallback.run();
        }
        closeDialog();
    }

    @FXML
    private void onClose() {
        logger.info("Close button clicked");
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        closeDialog();
    }

    private void closeDialog() {
        if (newGameButton != null && newGameButton.getScene() != null) {
            newGameButton.getScene().getWindow().hide();
        }
    }
}

