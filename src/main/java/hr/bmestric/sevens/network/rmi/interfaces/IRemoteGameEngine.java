package hr.bmestric.sevens.network.rmi.interfaces;

import hr.bmestric.sevens.engine.InvalidMoveException;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRemoteGameEngine extends Remote {

    void startNewGame(List<Player> players) throws RemoteException;

    GameState getGameState() throws RemoteException;

    GameState playCards(String playerId, List<Card> cards) throws RemoteException, InvalidMoveException;

    void passTurn(String playerId) throws RemoteException, InvalidMoveException;

    void endGame(boolean cancelled) throws RemoteException;

    void reset() throws RemoteException;

    void restoreState(GameState state) throws RemoteException;

    void registerClient(String clientId, IGameStateCallback callback) throws RemoteException;

    void registerPlayer(Player player) throws RemoteException;

    void unregisterClient(String clientId) throws RemoteException;
}
