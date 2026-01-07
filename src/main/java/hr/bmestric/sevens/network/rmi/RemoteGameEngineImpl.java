package hr.bmestric.sevens.network.rmi;

import hr.bmestric.sevens.engine.GameEngine;
import hr.bmestric.sevens.engine.InvalidMoveException;
import hr.bmestric.sevens.engine.interfaces.IGameEngine;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.network.rmi.interfaces.IGameStateCallback;
import hr.bmestric.sevens.network.rmi.interfaces.IRemoteGameEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteGameEngineImpl extends UnicastRemoteObject implements IRemoteGameEngine {

    private static final Logger logger = LoggerFactory.getLogger(RemoteGameEngineImpl.class);

    private final transient IGameEngine gameEngine;
    private final transient ConcurrentMap<String, IGameStateCallback> clients;
    private final List<Player> registeredPlayers;

    public RemoteGameEngineImpl() throws RemoteException {
        super();
        this.gameEngine = new GameEngine();
        this.clients = new ConcurrentHashMap<>();
        this.registeredPlayers = new ArrayList<>();
        logger.info("RemoteGameEngine initialized");
    }
    @Override
    public void startNewGame(List<Player> players) throws RemoteException {
        try {
            logger.info("Starting new game with {} players", players.size());
            gameEngine.startNewGame(players);
            notifyClients();
        } catch (Exception e) {
            throw new RemoteException("Failed to start new game", e);
        }
    }

    private void notifyClients() {
        GameState state = gameEngine.getState();
        if(state == null) return;

        clients.forEach((clientId, callback) -> {
            try {
                callback.onStateChanged(state);
            } catch (Exception e) {
                logger.error("Error notifying client {}: {}", clientId, e);
                clients.remove(clientId);
            }
        });
    }

    @Override
    public GameState getGameState() throws RemoteException {
        return gameEngine.getState();
    }

    @Override
    public GameState playCards(String playerId, List<Card> cards) throws RemoteException, InvalidMoveException {
        try {
            logger.info("Player {} plays {} cards", playerId, cards.size());
            GameState state = gameEngine.playCards(playerId, cards);
            notifyClients();
            return state;
        } catch (InvalidMoveException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to play cards", e);
        }
    }

    @Override
    public void passTurn(String playerId) throws RemoteException, InvalidMoveException {
        try {
            logger.info("Player {} passes turn", playerId);
            gameEngine.passTurn(playerId);
            notifyClients();
        } catch (InvalidMoveException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Failed to pass turn", e);
        }
    }

    @Override
    public void endGame(boolean cancelled) throws RemoteException {
        try {
            logger.info("Ending game, cancelled={}", cancelled);
            gameEngine.endGame(cancelled);
            notifyClients();
        } catch (Exception e) {
            throw new RemoteException("Failed to end game", e);
        }
    }

    @Override
    public void reset() throws RemoteException {
        try {
            logger.info("Resetting game engine");
            gameEngine.resetGame();
            notifyClients();
        } catch (Exception e) {
            throw new RemoteException("Failed to reset game", e);
        }
    }

    @Override
    public void restoreState(GameState state) throws RemoteException {
        try {
            logger.info("Restoring game state");
            gameEngine.restoreState(state);
            notifyClients();
            logger.info("Restored game state");
        } catch (Exception e) {
            throw new RemoteException("Failed to restore game state", e);
        }
    }

    @Override
    public void registerClient(String clientId, IGameStateCallback callback) throws RemoteException {
        logger.info("Registering client {}", clientId);
        clients.put(clientId, callback);

        try {
            GameState state = gameEngine.getState();
            if(state != null) {
                callback.onStateChanged(state);
            }
        } catch (Exception e) {
            logger.error("Error sending initial state to client {}", clientId, e);
        }
    }

    @Override
    public void registerPlayer(Player player) throws RemoteException {
        logger.info("Registering player {}", player.getDisplayName());

        boolean alreadyRegistered = registeredPlayers.stream()
                .anyMatch(p -> p.getId().equals(player.getId()));

        if(!alreadyRegistered) {
            registeredPlayers.add(player);
            logger.info("Player added. Total players: {}", registeredPlayers.size());
        }

        if(registeredPlayers.size() == 2) {
            try {
                logger.info("2 playeres registered, starting game automatically");
                gameEngine.startNewGame(registeredPlayers);
                notifyClients();
                logger.info("Game started successfully");
            } catch (Exception e) {
                throw new RemoteException("Failed to start game automatically", e);
            }
        }
    }

    @Override
    public void unregisterClient(String clientId) throws RemoteException {
        logger.info("Unregistering client {}", clientId);
        clients.remove(clientId);
    }
}
