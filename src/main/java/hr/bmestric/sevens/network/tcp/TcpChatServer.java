package hr.bmestric.sevens.network.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpChatServer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TcpChatServer.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final int port;
    private final Set<ClientHandler> clients;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public TcpChatServer(int port) {
        this.port = port;
        this.clients = ConcurrentHashMap.newKeySet();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "ChatServer-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        logger.info("TCP Chat Server started on port {}", port);

        Thread acceptThread = new Thread(this::acceptConnections, "ChatServer-Acceptor");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void acceptConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: {}", clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                executorService.execute(handler);

            } catch (SocketException e) {
                if (running) {
                    logger.error("Socket error in accept loop", e);
                }
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client connection", e);
                }
            }
        }
    }

    private void broadcast(String message, ClientHandler sender) {
        logger.debug("Broadcasting message: {}", message);

        for (ClientHandler client : clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }

    @Override
    public void close() {
        logger.info("Shutting down TCP Chat Server");
        running = false;

        for (ClientHandler client : clients) {
            client.close();
        }
        clients.clear();

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket", e);
            }
        }

        executorService.shutdown();
        logger.info("TCP Chat Server shut down");
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String playerName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                String firstLine = reader.readLine();
                if (firstLine != null && firstLine.startsWith("NAME:")) {
                    playerName = firstLine.substring(5);
                    logger.info("Player identified: {}", playerName);
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("MSG:")) {
                        String message = line.substring(4);
                        String timestamp = LocalTime.now().format(TIME_FORMATTER);
                        String formattedMessage = String.format("[%s] %s: %s", timestamp, playerName, message);

                        logger.info("Chat message from {}: {}", playerName, message);
                        broadcast(formattedMessage, this);
                    }
                }

            } catch (SocketException e) {
                logger.debug("Client disconnected: {}", playerName);
            } catch (IOException e) {
                logger.error("Error handling client: {}", playerName, e);
            } finally {
                close();
                clients.remove(this);
                logger.info("Client handler closed: {}", playerName);
            }
        }

        public void send(String message) {
            if (writer != null) {
                writer.println(message);
            }
        }

        public void close() {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                logger.error("Error closing client resources", e);
            }
        }
    }
}

