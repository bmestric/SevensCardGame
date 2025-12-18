package hr.bmestric.sevens.session;

import hr.bmestric.sevens.engine.InvalidMoveException;
import hr.bmestric.sevens.engine.interfaces.IGameEngine;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.session.interfaces.GameSession;

import java.util.List;
import java.util.Objects;

public class LocalGameSession implements GameSession {

    private final IGameEngine engine;
    private volatile StateListener stateListener;

    public LocalGameSession(IGameEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    @Override
    public GameState getState() {
        return engine.getState();
    }

    @Override
    public void playCard(String playerId, Card card) throws InvalidMoveException {
        engine.playCards(playerId, List.of(card));
        emit();
    }

    @Override
    public void passTurn(String playerId) throws InvalidMoveException {
        engine.passTurn(playerId);
        emit();
    }

    @Override
    public void restoreState(GameState state) {
        engine.restoreState(state);
        emit();
    }

    @Override
    public void resetAndRestartIfPossible() {
        GameState state = engine.getState();
        if (state == null || state.getPlayers() == null || state.getPlayers().size() != 2) {
            return;
        }
        List<Player> players = state.getPlayers();
        engine.resetGame();
        engine.startNewGame(players);
        emit();
    }

    @Override
    public void endGame(boolean cancelled) {
        engine.endGame(cancelled);
        emit();
    }

    @Override
    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    private void emit() {
        StateListener listener = stateListener;
        if (listener != null) {
            listener.onStateChanged(engine.getState());
        }
    }

    @Override
    public void close() {
        //nothing to clean up
    }
}
