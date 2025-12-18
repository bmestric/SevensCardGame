package hr.bmestric.sevens.events;

import hr.bmestric.sevens.model.GameState;

public class StateChangedEvent extends GameEvent {
    private final GameState gameState;
    private final String changeDescription;

    public StateChangedEvent(GameState gameState, String changeDescription) {
        super("STATE_CHANGED");
        this.gameState = gameState;
        this.changeDescription = changeDescription;
    }

    public GameState getGameState() {
        return gameState;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    @Override
    public String toString() {
        return String.format("StateChangedEvent{description='%s', status=%s, timestamp=%s}",
                changeDescription, gameState.getGameStatus(), getTimestamp());
    }
}
