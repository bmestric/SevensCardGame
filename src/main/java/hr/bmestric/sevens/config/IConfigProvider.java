package hr.bmestric.sevens.config;

import java.io.IOException;
import java.nio.file.Path;

public interface IConfigProvider {
    GameConfiguration loadConfig(Path configPath) throws IOException;
    void saveConfig(GameConfiguration configuration, Path configPath) throws IOException;
    GameConfiguration loadDefaultConfig();
}
