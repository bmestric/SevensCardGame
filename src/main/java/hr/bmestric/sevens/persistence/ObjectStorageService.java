package hr.bmestric.sevens.persistence;

import hr.bmestric.sevens.model.GameState;
import hr.bmestric.sevens.persistence.interfaces.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ObjectStorageService implements IStorageService {
    private static final Logger logger = LoggerFactory.getLogger(ObjectStorageService.class);

    @Override
    public void saveGame(GameState gameState, Path filePath) throws IOException {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("FilePath cannot be null");
        }

        logger.info("Saving game state to: {}", filePath);

        // Ensure parent directory exists
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(gameState);
            logger.info("Game state saved successfully");
        } catch (IOException e) {
            logger.error("Failed to save game state", e);
            throw e;
        }
    }

    @Override
    public GameState loadGame(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("FilePath cannot be null");
        }
        if (!Files.exists(filePath)) {
            throw new IOException("Save file not found: " + filePath);
        }

        logger.info("Loading game state from: {}", filePath);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            GameState loadedGameState = (GameState) ois.readObject();
            logger.info("Game state loaded successfully");
            return loadedGameState;
        } catch (FileNotFoundException e) {
            logger.error("Save file not found: {}", filePath, e);
            throw new IOException("Save file not found: " + filePath, e);
        } catch (ClassNotFoundException e) {
            logger.error("Failed to deserialize game state - class not found", e);
            throw new IOException("Failed to load game state - incompatible save file", e);
        } catch (IOException e) {
            logger.error("Failed to load game state", e);
            throw e;
        }
    }

    @Override
    public String getFormat() {
        return "dat";
    }
}
