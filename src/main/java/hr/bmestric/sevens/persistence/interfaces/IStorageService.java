package hr.bmestric.sevens.persistence.interfaces;

import hr.bmestric.sevens.model.GameState;

import java.io.IOException;
import java.nio.file.Path;

public interface IStorageService {
    void saveGame(GameState gameState, Path filePath) throws IOException;
    GameState loadGame(Path filePath) throws IOException, ClassNotFoundException;
    String getFormat();
}
