package hr.bmestric.sevens.network.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * TCP client for chat communication.
 * Maintains a socket connection and uses a separate thread to read incoming messages.
 */
public class TcpChatClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TcpChatClient.class);

    private final String host;
    private final int port;
    private final String playerName;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread readerThread;
    private volatile boolean running;

    private MessageListener messageListener;

    public TcpChatClient(String host, int port, String playerName) {
        this.host = host;
        this.port = port;
        this.playerName = playerName;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        running = true;

        // Send player name as first message
        writer.println("NAME:" + playerName);

        // Start reader thread
        readerThread = new Thread(this::readMessages, "ChatClient-Reader-" + playerName);
        readerThread.setDaemon(true);
        readerThread.start();

        logger.info("Connected to chat server at {}:{} as {}", host, port, playerName);
    }

    private void readMessages() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                logger.debug("Received chat message: {}", line);

                if (messageListener != null) {
                    // Parse message format: [HH:mm] PlayerName: message
                    messageListener.onMessageReceived(line);
                }
            }
        } catch (SocketException e) {
            if (running) {
                logger.error("Socket closed while reading", e);
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Error reading from server", e);
            }
        } finally {
            logger.info("Reader thread stopped for {}", playerName);
        }
    }

    public void sendMessage(String message) throws IOException {
        if (writer != null && running) {
            writer.println("MSG:" + message);
            logger.debug("Sent chat message: {}", message);
        } else {
            throw new IOException("Not connected to chat server");
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void close() {
        logger.info("Closing TCP chat client for {}", playerName);
        running = false;

        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.error("Error closing chat client", e);
        }

        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
    }

    @FunctionalInterface
    public interface MessageListener {
        void onMessageReceived(String formattedMessage);
    }
}

