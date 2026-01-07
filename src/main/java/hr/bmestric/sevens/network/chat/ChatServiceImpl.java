package hr.bmestric.sevens.network.chat;

import hr.bmestric.sevens.network.chat.interfaces.IChatListener;
import hr.bmestric.sevens.network.chat.interfaces.IChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServiceImpl extends UnicastRemoteObject implements IChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final transient ConcurrentMap<String, IChatListener> listeners;

    public ChatServiceImpl() throws RemoteException {
        super();
        this.listeners = new ConcurrentHashMap<>();
        logger.info("ChatService initialized");
    }

    @Override
    public void sendMessage(String fromPlayerId, String fromPlayerName, String message) throws RemoteException {
        logger.info("Chat message from {}: {}", fromPlayerName, message);

        String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        listeners.forEach((playerId, listener) -> {
            if (!playerId.equals(fromPlayerId)) {
                try {
                    listener.onMessageReceived(fromPlayerName, message, timestamp);
                    logger.debug("Message delivered to player: {}", playerId);
                } catch (Exception e) {
                    logger.error("Failed to deliver message to player: {}", playerId, e);
                    listeners.remove(playerId);
                }
            }
        });
    }

    @Override
    public void registerChatListener(String playerId, IChatListener listener) throws RemoteException {
        logger.info("Registering chat listener for player: {}", playerId);
        listeners.put(playerId, listener);
    }

    @Override
    public void unregisterChatListener(String playerId) throws RemoteException {
        logger.info("Unregistering chat listener for player: {}", playerId);
        listeners.remove(playerId);
    }
}
