package hr.bmestric.sevens.session;

import hr.bmestric.sevens.network.tcp.TcpChatClient;
import hr.bmestric.sevens.session.interfaces.IChatSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TcpChatSession implements IChatSession {
    private static final Logger logger = LoggerFactory.getLogger(TcpChatSession.class);

    private final TcpChatClient client;
    private volatile MessageListener messageListener;

    public TcpChatSession(String host, int port, String playerName) {
        this.client = new TcpChatClient(host, port, playerName);
    }

    public void connect() throws IOException {
        client.connect();

        // Forward messages from TCP client to session listener
        client.setMessageListener(formattedMessage -> {
            MessageListener listener = messageListener;
            if (listener != null) {
                String[] parts = formattedMessage.split(":", 3);
                if (parts.length >= 3) {
                    String timestamp = parts[0].replaceAll("[\\[\\]]", "").trim();
                    String playerName = parts[1].trim();
                    String message = parts[2].trim();
                    listener.onMessage(playerName, message, timestamp);
                }
            }
        });

        logger.info("TCP chat session connected");
    }

    @Override
    public void sendMessage(String fromPlayerId, String fromPlayerName, String message) throws IOException {
        client.sendMessage(message);
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void close() {
        client.close();
        logger.info("TCP chat session closed");
    }
}

