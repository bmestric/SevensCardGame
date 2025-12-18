package hr.bmestric.sevens.session.interfaces;

import hr.bmestric.sevens.engine.InvalidMoveException;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;

import java.io.Closeable;
import java.rmi.RemoteException;

/**
 * Thin facade over a game runtime (local or remote). Designed to keep JavaFX controllers
 * free from networking, callback registration, and persistence concerns.
 */
public interface GameSession extends Closeable {

    GameState getState() throws RemoteException;

    void playCard(String playerId, Card card) throws RemoteException, InvalidMoveException;

    void passTurn(String playerId) throws RemoteException, InvalidMoveException;

    void restoreState(GameState state) throws RemoteException;

    void resetAndRestartIfPossible() throws RemoteException;

    void endGame(boolean cancelled) throws RemoteException;

    void setStateListener(StateListener listener);

    @Override
    void close();

    @FunctionalInterface
    interface StateListener {
        void onStateChanged(GameState state);
    }
}
