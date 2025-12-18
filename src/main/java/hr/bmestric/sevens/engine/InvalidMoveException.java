package hr.bmestric.sevens.engine;

public class InvalidMoveException extends RuntimeException {
    private final String playerId;
    private final String moveDescription;

    public InvalidMoveException(String message) {
        super(message);
        this.playerId = null;
        this.moveDescription = null;
    }

    public InvalidMoveException(String message, String playerId, String moveDescription) {
        super(message);
        this.playerId = playerId;
        this.moveDescription = moveDescription;
    }

    public InvalidMoveException(String message, Throwable cause) {
        super(message, cause);
        this.playerId = null;
        this.moveDescription = null;
    }


    public String getPlayerId() {
        return playerId;
    }


    public String getMoveDescription() {
        return moveDescription;
    }
}
