package hr.bmestric.sevens.session;

import hr.bmestric.sevens.engine.InvalidMoveException;
import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.network.rmi.interfaces.IGameStateCallback;
import hr.bmestric.sevens.network.rmi.interfaces.IRemoteGameEngine;
import hr.bmestric.sevens.session.interfaces.IGameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class RmiGameSession implements IGameSession {
    private static final Logger logger = LoggerFactory.getLogger(RmiGameSession.class);

    private final IRemoteGameEngine remoteEngine;
    private final String clientId;

    private volatile StateListener stateListener;

    private IGameStateCallback callbackImpl;
    private IGameStateCallback callbackStub;

    public RmiGameSession(IRemoteGameEngine remoteEngine, String clientId) {
        this.remoteEngine = remoteEngine;
        this.clientId = clientId;
    }

    public void connect() throws RemoteException {
        if(callbackImpl != null) return;

        callbackImpl = new IGameStateCallback() {
            @Override
            public void onStateChanged(GameState newState) throws RemoteException {
                StateListener listener = stateListener;
                if(listener != null) {
                    listener.onStateChanged(newState);
                }
            }
        };

        try{
            callbackStub = (IGameStateCallback)  UnicastRemoteObject.exportObject(callbackImpl, 0);
            remoteEngine.registerClient(clientId, callbackStub);
            logger.info("RMI session registered callback for client {}", clientId);
        } catch (RemoteException e) {
            cleaunpCallback();
            throw e;
        }

        try {
            GameState state = remoteEngine.getGameState();
            StateListener listener = stateListener;
            if(state != null && listener != null) {
                listener.onStateChanged(state);
            }
        } catch (Exception e) {
            logger.debug("Unable to fetch inital state",e);
        }
    }

    private void cleaunpCallback() {
        if(callbackImpl != null) {
            try {
                UnicastRemoteObject.unexportObject(callbackImpl, true);
            } catch (Exception e) {
                logger.debug("Error unexporting RMI callback",e);
            }
        }
        callbackImpl = null;
        callbackStub = null;
    }

    @Override
    public GameState getState() throws RemoteException {
        return remoteEngine.getGameState();
    }

    @Override
    public void playCard(String playerId, Card card) throws RemoteException, InvalidMoveException {
        remoteEngine.playCards(playerId, List.of(card));
    }

    @Override
    public void passTurn(String playerId) throws RemoteException, InvalidMoveException {
        remoteEngine.passTurn(playerId);
    }

    @Override
    public void restoreState(GameState state) throws RemoteException {
        remoteEngine.restoreState(state);
    }

    @Override
    public void resetAndRestartIfPossible() throws RemoteException {
        GameState state = remoteEngine.getGameState();
        if(state == null || state.getPlayers() == null || state.getPlayers().size() != 2) {
            return;
        }
        List<Player> players = state.getPlayers();
        remoteEngine.reset();
        remoteEngine.startNewGame(players);
    }

    @Override
    public void endGame(boolean cancelled) throws RemoteException {
        remoteEngine.endGame(cancelled);
    }

    @Override
    public void setStateListener(StateListener listener) {
        this.stateListener = listener;
    }

    @Override
    public void close() {
        try {
            remoteEngine.unregisterClient(clientId);
        } catch (RemoteException e) {
            logger.debug("Error unregistering RMI client",e);
        }
        cleaunpCallback();
    }
}
