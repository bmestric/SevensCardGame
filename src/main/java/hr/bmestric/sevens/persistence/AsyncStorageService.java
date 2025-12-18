package hr.bmestric.sevens.persistence;

import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.persistence.interfaces.IStorageService;
import hr.bmestric.sevens.util.ThreadingHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class AsyncStorageService {
    private final IStorageService delegate;
    private final ExecutorService executor;

    public AsyncStorageService(IStorageService delegate) {
        this(delegate, ThreadingHelper.createSingleThreadExecutor("Storage-IO"));
    }

    public AsyncStorageService(IStorageService delegate, ExecutorService executor) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public CompletableFuture<Void> saveGameAsync(GameState gameState, Path filePath) {
        return CompletableFuture.runAsync(() -> {
            try {
                delegate.saveGame(gameState, filePath);
            } catch (IOException e) {
                throw new StorageRuntimeException(e);
            }
        }, executor);
    }

    public CompletableFuture<GameState> loadGameAsync(Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return delegate.loadGame(filePath);
            } catch (IOException | ClassNotFoundException e) {
                throw new StorageRuntimeException(e);
            }
        }, executor);
    }

    public void shutdown() {
        ThreadingHelper.shutdownGracefully(executor, 3);
    }

    /**
     * Wrap checked IO exceptions into an unchecked form suitable for CompletableFuture.
     */
    public static class StorageRuntimeException extends RuntimeException {
        public StorageRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
