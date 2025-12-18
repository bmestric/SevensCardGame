package hr.bmestric.sevens.model;

import hr.bmestric.sevens.model.enums.PlayerType;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String displayName;
    private final PlayerType playerType;
    private final Hand hand;
    private int score;

    public Player(String displayName, PlayerType playerType) {
        this(UUID.randomUUID().toString(),displayName,playerType);
    }

    public Player(String id, String displayName, PlayerType playerType) {
        if(id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Player ID cannot be null or empty");
        }
        if(displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }
        this.id = id;
        this.displayName = displayName;
        this.playerType = Objects.requireNonNull(playerType, "Player type cannot be null");
        this.hand = new Hand();
        this.score = 0;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public Hand getHand() {
        return hand;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        if(points < 0) {
            throw new IllegalArgumentException("Points to add cannot be negative");
        }
        this.score += points;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(displayName, player.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(displayName);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", hand=" + hand +
                ", score=" + score +
                '}';
    }
}
