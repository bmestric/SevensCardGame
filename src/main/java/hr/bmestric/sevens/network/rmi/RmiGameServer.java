package hr.bmestric.sevens.network.rmi;

import hr.bmestric.sevens.config.GameConfiguration;
import hr.bmestric.sevens.network.chat.ChatServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiGameServer {
    private static final Logger logger = LoggerFactory.getLogger(RmiGameServer.class);
    private static final GameConfiguration gameConfiguration = new GameConfiguration();
    private static final int DEFAULT_PORT = gameConfiguration.getRmiRegistryPort();
    private static final String SERVICE_NAME = "SevensGameEngine";

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.error("Invalid port number: {}", args[0]);
                System.err.println("Usage: java RmiGameServer [port]");
                System.exit(1);
            }
        }

            try {
                logger.info("Starting RMI Game Server on port {}", port);

                Registry registry;
                try {
                    registry = LocateRegistry.createRegistry(port);
                    registry.list();
                    logger.info("Found existing RMI registry on port {}, reusing it", port);
                    System.out.println(" using existing RMI registry on port " + port);
                } catch (Exception e) {
                    logger.info("No existing registry found, creating new RMI registry on port {}", port);
                    registry = LocateRegistry.createRegistry(port);
                    logger.info("RMI registry created on port {}", port);
                }
                RemoteGameEngineImpl gameEngine = new RemoteGameEngineImpl();
                ChatServiceImpl chatService = new ChatServiceImpl();

                registry.rebind(SERVICE_NAME, gameEngine);
                registry.rebind("SevensChatService", chatService);

                logger.info("Game Engine bound to registry as '{}'", SERVICE_NAME);
                logger.info("Chat Service bound to registry as 'SevensChatService'");
                System.out.println("╔═══════════════════════════════════════════════════╗");
                System.out.println("║                                                   ║");
                System.out.println("║        RMI GAME SERVER STARTED                    ║");
                System.out.println("║                                                   ║");
                System.out.println("╚═══════════════════════════════════════════════════╝");
                System.out.println();
                System.out.println("  Port: " + port);
                System.out.println("  Service: " + SERVICE_NAME);
                System.out.println();
                System.out.println("  Waiting for clients to connect...");
                System.out.println("  Press Ctrl+C to stop the server");
                System.out.println();

                final Registry finalRegistry = registry;

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        logger.info("Shutting down RMI Game Server");
                        finalRegistry.unbind(SERVICE_NAME);
                        System.out.println("\nServer shut down gracefully.");
                    } catch (Exception e) {
                        logger.error("Error during server shutdown", e);
                    }
                }));

                Object lock = new Object();
                synchronized (lock) {
                    lock.wait();
                }
            } catch (Exception e) {
                logger.error("Failed to start RMI Game Server", e);
                System.err.println("\n╔═══════════════════════════════════════════════════╗");
                System.err.println("║              ERROR STARTING SERVER                ║");
                System.err.println("╚═══════════════════════════════════════════════════╝");
                System.err.println("\nError: " + e.getMessage());

                if (e.getMessage() != null && e.getMessage().contains("Port already in use")) {
                    System.err.println("\nPort " + port + " is already in use!");
                    System.err.println("\nSolutions:");
                    System.err.println("  1. Stop the existing process using port " + port);
                    System.err.println("     Windows: netstat -ano | findstr :" + port);
                    System.err.println("              taskkill /F /PID <PID>");
                    System.err.println("  2. Use a different port:");
                    System.err.println("     java RmiGameServer <different_port>");
                    System.err.println("     Example: java RmiGameServer 1100");
                }

                System.exit(1);
            }
        }
    }
