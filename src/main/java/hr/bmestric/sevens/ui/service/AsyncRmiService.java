package hr.bmestric.sevens.ui.service;

import hr.bmestric.sevens.model.Card;
import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.session.interfaces.IGameSession;
import hr.bmestric.sevens.util.ThreadingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AsyncRmiService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncRmiService.class);

    private final ExecutorService rmiExecutor;

    public AsyncRmiService() {
        this(ThreadingHelper.createFixedThreadPool(2, "RMI-Worker"));
    }

    public AsyncRmiService(ExecutorService executor) {
        this.rmiExecutor = executor;
        logger.info("AsyncRmiService initialized with executor: {}", executor.getClass().getSimpleName());
    }

    public CompletableFuture<GameState> getStateAsync(IGameSession session) {
        logger.debug("Fetching game state asynchronously");
        return CompletableFuture.supplyAsync(() -> {
            try {
                return session.getState();
            } catch (Exception e) {
                logger.error("Error fetching game state", e);
                throw new AsyncRmiException("Failed to get game state", e);
            }
        }, rmiExecutor);
    }

    public CompletableFuture<Void> playCardAsync(IGameSession session, String playerId, Card card) {
        logger.debug("Playing card asynchronously: {} by {}", card, playerId);
        return CompletableFuture.runAsync(() -> {
            try {
                session.playCard(playerId, card);
            } catch (Exception e) {
                logger.error("Error playing card", e);
                throw new AsyncRmiException("Failed to play card", e);
            }
        }, rmiExecutor);
    }

    public CompletableFuture<Void> passTurnAsync(IGameSession session, String playerId) {
        logger.debug("Passing turn asynchronously for player: {}", playerId);
        return CompletableFuture.runAsync(() -> {
            try {
                session.passTurn(playerId);
            } catch (Exception e) {
                logger.error("Error passing turn", e);
                throw new AsyncRmiException("Failed to pass turn", e);
            }
        }, rmiExecutor);
    }

    public CompletableFuture<Void> resetGameAsync(IGameSession session) {
        logger.debug("Resetting game asynchronously");
        return CompletableFuture.runAsync(() -> {
            try {
                session.resetAndRestartIfPossible();
            } catch (Exception e) {
                logger.error("Error resetting game", e);
                throw new AsyncRmiException("Failed to reset game", e);
            }
        }, rmiExecutor);
    }

    public CompletableFuture<Void> restoreStateAsync(IGameSession session, GameState state) {
        logger.debug("Restoring game state asynchronously");
        return CompletableFuture.runAsync(() -> {
            try {
                session.restoreState(state);
            } catch (Exception e) {
                logger.error("Error restoring state", e);
                throw new AsyncRmiException("Failed to restore state", e);
            }
        }, rmiExecutor);
    }

    public void shutdown() {
        logger.info("Shutting down AsyncRmiService");
        ThreadingHelper.shutdownGracefully(rmiExecutor, 5);
    }

    public static class AsyncRmiException extends RuntimeException {
        public AsyncRmiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

