package hr.bmestric.sevens.model;

import hr.bmestric.sevens.model.enums.GameStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int WINNING_SCORE = 4;

    private final String gameId;
    private final List<Player> players;
    private final Deck deck;
    private final Trick trick;
    private final List<Trick> completedTricks;

    private String currentTurnPlayerId;
    private String lastTrickWinnerId;
    private GameStatus gameStatus;
    private Instant createdAt;
    private Instant lastModifiedAt;

    public GameState(List<Player> players) {
        this(UUID.randomUUID().toString(), players);
    }

    public GameState(String gameId, List<Player> players) {
        if (gameId == null || gameId.trim().isEmpty()) {
            throw new IllegalArgumentException("Game ID cannot be null or empty");
        }
        if (players == null || players.size() != 2) {
            throw new IllegalArgumentException("Game requires exactly 2 players");
        }

        this.gameId = gameId;
        this.players = new ArrayList<>(players);
        this.deck = new Deck();
        this.trick = new Trick();
        this.completedTricks = new ArrayList<>();
        this.gameStatus = GameStatus.NOT_STARTED;
        this.createdAt = Instant.now();
        this.lastModifiedAt = Instant.now();
    }

    public String getGameId() {
        return gameId;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Optional<Player> getPlayerById(String playerId) {
        return players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst();
    }

    public Optional<Player> getOpponent(String playerId) {
        return players.stream()
                .filter(p -> !p.getId().equals(playerId))
                .findFirst();
    }

    public Deck getDeck() {
        return deck;
    }

    public Trick getTrick() {
        return trick;
    }

    public List<Trick> getCompletedTricks() {
        return Collections.unmodifiableList(completedTricks);
    }

    public String getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(String currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
        updateLastModified();
    }

    public String getLastTrickWinnerId() {
        return lastTrickWinnerId;
    }

    public void setLastTrickWinnerId(String lastTrickWinnerId) {
        this.lastTrickWinnerId = lastTrickWinnerId;
        updateLastModified();
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        updateLastModified();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void archiveCurrentTrick() {
        if(trick.hasCards()) {
            Trick archived = new Trick();
            trick.getCards().forEach(card ->
                    archived.addCard(card, trick.getLeadingPlayerId()));
            completedTricks.add(archived);
            trick.clear();
        }
    }

    private void updateLastModified() {
        this.lastModifiedAt = Instant.now();
    }

    public boolean isGameOver() {
        return gameStatus == GameStatus.COMPLETED || gameStatus == GameStatus.CANCELLED;
    }

    public boolean isInProgress() {
        return gameStatus == GameStatus.IN_PROGRESS;
    }

    public Optional<Player> getWinner() {
        if(!isGameOver() && gameStatus != GameStatus.IN_PROGRESS) {
           return Optional.empty();
        }

        Player player1 = players.get(0);
        Player player2 = players.get(1);

        if(player1.getScore() > WINNING_SCORE && player2.getScore() <= WINNING_SCORE) {
            return Optional.of(player1);
        }

        if(player2.getScore() > WINNING_SCORE && player1.getScore() <= WINNING_SCORE) {
            return Optional.of(player2);
        }

        if (player1.getScore() == WINNING_SCORE && player2.getScore() == WINNING_SCORE) {
            if (lastTrickWinnerId != null) {
                return getPlayerById(lastTrickWinnerId);
            }
        }

        return Optional.empty();
    }
}
