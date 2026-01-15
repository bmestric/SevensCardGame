package hr.bmestric.sevens.ui.model;

import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;

import java.util.List;
import java.util.Map;

/**
 * View model for UI rendering.
 * Contains pre-processed data ready for UI display.
 * Prepared in background thread to avoid blocking JavaFX thread.
 */
public class UIViewModel {
    private GameState gameState;
    private String statusText;
    private String deckInfo;
    private String currentPlayerInfo;
    private List<Card> localPlayerHand;
    private List<Card> opponentHand;
    private List<Card> trickCards;
    private Map<String, Integer> playerScores;
    private boolean localPlayerTurn;
    private boolean gameOver;

    public UIViewModel() {
    }

    public UIViewModel(GameState gameState) {
        this.gameState = gameState;
    }

    // Getters and setters

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getDeckInfo() {
        return deckInfo;
    }

    public void setDeckInfo(String deckInfo) {
        this.deckInfo = deckInfo;
    }

    public String getCurrentPlayerInfo() {
        return currentPlayerInfo;
    }

    public void setCurrentPlayerInfo(String currentPlayerInfo) {
        this.currentPlayerInfo = currentPlayerInfo;
    }

    public List<Card> getLocalPlayerHand() {
        return localPlayerHand;
    }

    public void setLocalPlayerHand(List<Card> localPlayerHand) {
        this.localPlayerHand = localPlayerHand;
    }

    public List<Card> getOpponentHand() {
        return opponentHand;
    }

    public void setOpponentHand(List<Card> opponentHand) {
        this.opponentHand = opponentHand;
    }

    public List<Card> getTrickCards() {
        return trickCards;
    }

    public void setTrickCards(List<Card> trickCards) {
        this.trickCards = trickCards;
    }

    public Map<String, Integer> getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(Map<String, Integer> playerScores) {
        this.playerScores = playerScores;
    }

    public boolean isLocalPlayerTurn() {
        return localPlayerTurn;
    }

    public void setLocalPlayerTurn(boolean localPlayerTurn) {
        this.localPlayerTurn = localPlayerTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}

