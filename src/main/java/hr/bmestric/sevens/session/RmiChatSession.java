package hr.bmestric.sevens.session;

import hr.bmestric.sevens.network.chat.interfaces.IChatListener;
import hr.bmestric.sevens.network.chat.interfaces.IChatService;
import hr.bmestric.sevens.session.interfaces.IChatSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

public class RmiChatSession implements IChatSession {
    private static final Logger logger = LoggerFactory.getLogger(RmiChatSession.class);

    private final IChatService chatService;
    private final String playerId;

    private volatile MessageListener messageListener;

    private IChatListener listenerImpl;
    private IChatListener listenerStub;

    public RmiChatSession(IChatService chatService, String playerId) {
        this.chatService = Objects.requireNonNull(chatService, "chatService");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    public void connect() throws RemoteException {
        if (listenerStub != null) {
            return;
        }

        listenerImpl = new IChatListener() {
            @Override
            public void onMessageReceived(String fromPlayerName, String message, String timestamp) throws RemoteException {
                MessageListener l = messageListener;
                if (l != null) {
                    l.onMessage(fromPlayerName, message, timestamp);
                }
            }
        };

        try {
            listenerStub = (IChatListener) UnicastRemoteObject.exportObject(listenerImpl, 0);
            chatService.registerChatListener(playerId, listenerStub);
            logger.info("Registered RMI chat listener for player {}", playerId);
        } catch (RemoteException e) {
            cleanupListener();
            throw e;
        }
    }

    @Override
    public void sendMessage(String fromPlayerId, String fromPlayerName, String message) throws RemoteException {
        chatService.sendMessage(fromPlayerId, fromPlayerName, message);
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void close() {
        try {
            chatService.unregisterChatListener(playerId);
        } catch (Exception e) {
            logger.debug("Error unregistering chat listener", e);
        }
        cleanupListener();
    }

    private void cleanupListener() {
        unexportListener();
        listenerImpl = null;
        listenerStub = null;
    }

    private void unexportListener() {
        if (listenerImpl != null) {
            try {
                UnicastRemoteObject.unexportObject(listenerImpl, true);
            } catch (Exception e) {
                logger.debug("Error unexporting chat listener", e);
            }
        }
    }
}
